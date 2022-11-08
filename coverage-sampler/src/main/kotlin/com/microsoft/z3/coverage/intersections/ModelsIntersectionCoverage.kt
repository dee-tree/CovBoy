package com.microsoft.z3.coverage.intersections

import com.sokolov.covboy.coverage.*
import com.sokolov.covboy.logger
import com.sokolov.covboy.prover.Assignment
import com.sokolov.covboy.prover.BaseProverEnvironment
import com.sokolov.covboy.prover.Status
import com.sokolov.covboy.prover.model.BoolModelAssignmentsImpl
import com.sokolov.covboy.prover.model.ModelAssignments
import com.sokolov.covboy.smt.isCertainBool
import com.sokolov.covboy.smt.isNot
import com.sokolov.covboy.smt.notOptimized
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sosy_lab.java_smt.api.BooleanFormula

class ModelsIntersectionCoverage(
    prover: BaseProverEnvironment,
    coveragePredicates: Collection<BooleanFormula>,
    val intersectionSize: Int = 2,
    private val nonChangedCoverageIterationsLimit: Int = 1
) : CoverageSampler(prover, coveragePredicates) {

    private var nonChangedCoverageIterations = 0

    private var uselessIntersectionModelsCoverage = 0
    private var usefulIntersectionModelsCoverage = 0


    override fun computeCoverage(
        coverModel: (ModelAssignments<BooleanFormula>) -> Set<AtomCoverageBase>,
        coverAtom: (assignment: Assignment<BooleanFormula>) -> AtomCoverageBase,
        onImpossibleAssignmentFound: (assignment: Assignment<BooleanFormula>) -> Unit
    ) {
        prover.push()
        do {
//            if (nonChangedCoverageIterations > 0)
//                logger().trace("Non-changed coverage iterations: $nonChangedCoverageIterations")
            var coverageChanged = false

            // if coverage not changed, let's try to add new assert
            if (nonChangedCoverageIterations >= nonChangedCoverageIterationsLimit) {
//                logger().debug("Jump to another atoms in the formula due to $nonChangedCoverageIterationsLimit times coverage didn't change")
                val semiUncoveredAtoms = atomsWithSingleUncoveredValue

//                logger().trace("atoms with single uncovered value: ${semiUncoveredAtoms.size}")

                if (semiUncoveredAtoms.isEmpty()) {
                    nonChangedCoverageIterations = 0
                    continue
                }

                val rememberedEnabledConstraints = prover
                    .filterSwitchableConstraints { it.enabled && it.tag.startsWith("ic.switchable") }
                    .onEach { prover.disableConstraint(it) }

                val semiUncoveredAtomsAsExpr = semiUncoveredAtoms.mergeWithOr(prover)

                val assertion = prover.addConstraint(semiUncoveredAtomsAsExpr, true, "ic.switchable.semiuncovered")

                when (prover.check(checkStatusId())) {
                    Status.SAT -> {
                        val modelCoverage =
                            coverModel(BoolModelAssignmentsImpl(prover.model, coveragePredicates, prover))
                        coverageChanged = modelCoverage.any { it !is EmptyAtomCoverage }
                    }
                    Status.UNSAT -> {
                        atomsWithSingleUncoveredValue
                            .map { Assignment(it.key, it.value) }
                            .forEach(onImpossibleAssignmentFound)
                    }
                    Status.UNKNOWN -> throw IllegalStateException("Unknown result of check")
                }

                // disable assertion, because it causes a strong distortion (hard assert on the atom!)
                prover.disableConstraint(assertion)

                rememberedEnabledConstraints.forEach(prover::enableConstraint)

            } else {
//                logger().debug("Extract intersections of atoms in $intersectionSize models")
                val currentBoundModels = modelsEnumerator.take(intersectionSize)

                currentBoundModels.forEach {
                    val currentModelCoverage = coverModel(it)
                    val thisModelChangedCoverage = currentModelCoverage.any { atomCoverage ->
                        atomCoverage !is EmptyAtomCoverage
                    }

                    if (thisModelChangedCoverage) usefulIntersectionModelsCoverage++
                    else uselessIntersectionModelsCoverage++

                    coverageChanged = coverageChanged || thisModelChangedCoverage
                }

                if (currentBoundModels.count() < intersectionSize) {
//                    println("Break in intersection take loop")
                    break
                }

                val intersection = currentBoundModels
                    .fold(
                        coveragePredicates.map { it to currentBoundModels.first().evaluate(it) }
                            .mapNotNull { if (it.second == null) null else Assignment(it.first, it.second!!) }
                            .toSet()
                    ) { acc, currentModel ->
                        acc.intersect(coveragePredicates.map { it to currentModel.evaluate(it) }
                            .mapNotNull { if (it.second == null) null else Assignment(it.first, it.second!!) }.toSet())
                    }.filter { it.value.isCertainBool(formulaManager.booleanFormulaManager) }

                if (intersection.isEmpty()) {
//                    logger().info("No intersection found")
                    continue
                }
//                logger().info("Found non-empty intersection consisting of ${intersection.size} atoms")

                val intersectionConstraint = intersection.mergeWithAnd(prover)

                val negatedIntersection = prover.fm.booleanFormulaManager.notOptimized(intersectionConstraint)
                val negIntersectionAssertion =
                    prover.addConstraint(negatedIntersection, true, "ic.switchable.negintersection")

                if (prover.check(checkStatusId()) == Status.UNSAT) {
                    // conflicted intersection found
                    resolveConflict(negIntersectionAssertion, onImpossibleAssignmentFound)
                }
            }

            if (coverageChanged) nonChangedCoverageIterations = 0 else nonChangedCoverageIterations++
        } while (!isCovered)

        prover.pop()

        logger().info("Traversed ${modelsEnumerator.traversedModelsCount} models")
        logger().info("Useful models coverage from intersection: $usefulIntersectionModelsCoverage models")
        logger().info("Useless models coverage from intersection: $uselessIntersectionModelsCoverage models")
        logger().info("Useful / useless coeff of models coverage from intersection: ${usefulIntersectionModelsCoverage.toDouble() / uselessIntersectionModelsCoverage}")
    }

    private fun resolveConflict(
        assertion: BooleanFormula,
        onImpossibleAssignmentFound: (assignment: Assignment<BooleanFormula>) -> Unit,
    ) {
//        logger().trace("Resolve conflict")
        val unsatCore = prover.unsatCoreWithAssumptions

        val customAssertionsFromCore = prover.filterSwitchableConstraints { it.original in unsatCore }

        // if only custom assertions conflict
        if (customAssertionsFromCore.size != 1 && unsatCore.all { it in customAssertionsFromCore }) {
//            logger().info("Assertions conflict: custom assertions")

            prover.filterSwitchableConstraints {
                it.tag.startsWith("ic.switchable")
                        && it.original !in customAssertionsFromCore
                        && it.original != assertion
            }.forEach(prover::disableConstraint)

        } else {
            // conflict with original assertions branch
//            logger().info("Assertions conflict: with original formula. Disable the assertion")
//            logger().trace("The assertion disabled: $assertion")

            // TODO: if it's 1, then disable all assertions and check(). if sat - impossible assignment found
            if (customAssertionsFromCore.size == 1) {

                checkOnNonSwitchableConflict(customAssertionsFromCore.first()) {
//                    logger().trace("Found unachievable value of atom: $assertion")

                    val (atom, value) = if (formulaManager.booleanFormulaManager.isNot(assertion))
                        prover.fm.booleanFormulaManager.notOptimized(assertion) to formulaManager.booleanFormulaManager.makeFalse()
                    else assertion to formulaManager.booleanFormulaManager.makeTrue()
                    onImpossibleAssignmentFound(Assignment(atom, value))
                }
            }
            prover.disableConstraint(assertion)
        }
    }

    fun checkOnNonSwitchableConflict(f: BooleanFormula, onOnlyNonSwitchableConflict: () -> Unit) {
        val enabledConstraints = prover.enabledSwitchableConstraints - f
        enabledConstraints.forEach(prover::disableConstraint)

        assert(f in prover.formulas)
        when (prover.check()) {
            Status.UNSAT -> { prover.disableConstraint(f); if(prover.check() == Status.SAT) onOnlyNonSwitchableConflict() }
            Status.UNKNOWN -> System.err.println("Unknown result in NonSwitchable formulas check()")
        }

        enabledConstraints.forEach(prover::enableConstraint)
    }
}