package com.sokolov.covboy.coverage.sampler.impl

import com.sokolov.covboy.coverage.AtomCoverageBase
import com.sokolov.covboy.coverage.EmptyAtomCoverage
import com.sokolov.covboy.coverage.sampler.CoverageSampler
import com.sokolov.covboy.logger
import com.sokolov.covboy.solvers.formulas.Constraint
import com.sokolov.covboy.solvers.formulas.NonSwitchableConstraint
import com.sokolov.covboy.solvers.formulas.SwitchableConstraint
import com.sokolov.covboy.solvers.formulas.asSwitchableConstraint
import com.sokolov.covboy.solvers.formulas.utils.isNot
import com.sokolov.covboy.solvers.formulas.utils.notOptimized
import com.sokolov.covboy.solvers.provers.Prover
import com.sokolov.covboy.solvers.provers.Status
import com.sokolov.covboy.utils.makeAssignment
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.Model

class ModelsIntersectionCoverageSampler(
    prover: Prover,
    coveragePredicates: Collection<BooleanFormula>,
    val intersectionSize: Int = 2,
    private val nonChangedCoverageIterationsLimit: Int = 1
) : CoverageSampler(prover, coveragePredicates) {

    private var nonChangedCoverageIterations = 0

    private var uselessIntersectionModelsCoverage = 0
    private var usefulIntersectionModelsCoverage = 0


    override fun computeCoverage(
        coverModel: (List<Model.ValueAssignment>) -> Set<AtomCoverageBase>,
        coverAtom: (assignment: Model.ValueAssignment) -> AtomCoverageBase,
        onImpossibleAssignmentFound: (assignment: Model.ValueAssignment) -> Unit
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
                    .switchableConstraints.filter { it.enabled && it.tag.startsWith("ic.switchable") }
                    .onEach { prover.disableConstraint(it) }

                val semiUncoveredAtomsAsExpr = prover.fm.booleanFormulaManager.or(semiUncoveredAtoms.map { it.assignmentAsFormula })

                val constraint = semiUncoveredAtomsAsExpr.asSwitchableConstraint(prover.fm, "ic.switchable.semiuncovered", true)
                prover.addConstraint(constraint)

                when (prover.checkSat()) {
                    Status.SAT -> {
                        val modelCoverage = coverModel(prover.modelAssignments)
                        coverageChanged = modelCoverage.any { it !is EmptyAtomCoverage }
                    }
                    Status.UNSAT -> {
                        atomsWithSingleUncoveredValue
                            .map {
                                (it.key as BooleanFormula to it.value as BooleanFormula)
                                    .makeAssignment(prover.fm.booleanFormulaManager)
                            }
                            .forEach(onImpossibleAssignmentFound)
                    }
                    Status.UNKNOWN -> throw IllegalStateException("Unknown result of check")
                }

                // disable assertion, because it causes a strong distortion (hard assert on the atom!)
                prover.disableConstraint(constraint)

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

                val getCovPredicatesAssignments = { model: List<Model.ValueAssignment> ->
                    model.filter { it.key in coveragePredicates }.toSet()
                }

                val intersection = currentBoundModels.fold(
                    getCovPredicatesAssignments(currentBoundModels.first())
                ) { acc, currentModel ->
                    acc.intersect(getCovPredicatesAssignments(currentModel))
                }

                if (intersection.isEmpty()) {
//                    logger().info("No intersection found")
                    continue
                }
//                logger().info("Found non-empty intersection consisting of ${intersection.size} atoms")

                val intersectionConstraint = prover.fm.booleanFormulaManager.and(intersection.map { it.assignmentAsFormula })

                val negatedIntersection = prover.fm.booleanFormulaManager.notOptimized(intersectionConstraint)
                val negIntersectionConstraint = negatedIntersection.asSwitchableConstraint(prover.fm, "ic.switchable.negintersection", true)
                    prover.addConstraint(negIntersectionConstraint)

                if (prover.checkSat() == Status.UNSAT) {
                    // conflicted intersection found
                    resolveConflict(negIntersectionConstraint, onImpossibleAssignmentFound)
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
        assertion: Constraint,
        onImpossibleAssignmentFound: (assignment: Model.ValueAssignment) -> Unit,
    ) {
//        logger().trace("Resolve conflict")
        val unsatCore = prover.getUnsatCoreConstraints()

        // if only custom assertions conflict
        if (unsatCore.size > 1 && unsatCore.all { it is SwitchableConstraint }) {
//        if (customAssertionsFromCore.size != 1 && unsatCore.all { it in customAssertionsFromCore }) {
//            logger().info("Assertions conflict: custom assertions")

            prover.switchableConstraints.filter {
                it.tag.startsWith("ic.switchable")
                        && it !in unsatCore
                        && it != assertion
            }.forEach(prover::disableConstraint)

        } else {
            // conflict with original assertions branch
//            logger().info("Assertions conflict: with original formula. Disable the assertion")
//            logger().trace("The assertion disabled: $assertion")

            // conflict with non-switchable constraint found
            if (unsatCore.any { it is NonSwitchableConstraint }) {

                checkOnNonSwitchableConflict(unsatCore.first()) {
//                    logger().trace("Found unachievable value of atom: $assertion")

                    val expr = if (assertion is SwitchableConstraint) assertion.original else assertion.asFormula
                    val (atom, value) = if (formulaManager.booleanFormulaManager.isNot(expr))
                        prover.fm.booleanFormulaManager.notOptimized(expr) to formulaManager.booleanFormulaManager.makeFalse()
                    else expr to formulaManager.booleanFormulaManager.makeTrue()
                    onImpossibleAssignmentFound((atom to value).makeAssignment(prover.fm.booleanFormulaManager))
                }
            }
            prover.disableConstraint(assertion)
        }
    }

    fun checkOnNonSwitchableConflict(f: Constraint, onOnlyNonSwitchableConflict: () -> Unit) {
        val enabledConstraints = prover.switchableConstraints.filter { it.enabled } - f
        enabledConstraints.forEach(prover::disableConstraint)

        when (prover.checkSat()) {
            Status.UNSAT -> { prover.disableConstraint(f); if(prover.checkSat() == Status.SAT) onOnlyNonSwitchableConflict() }
            Status.UNKNOWN -> System.err.println("Unknown result in NonSwitchable formulas check()")
        }

        enabledConstraints.forEach(prover::enableConstraint)
    }
}