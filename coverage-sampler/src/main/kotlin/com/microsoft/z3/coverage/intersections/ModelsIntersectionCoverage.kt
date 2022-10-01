package com.microsoft.z3.coverage.intersections

import com.microsoft.z3.*
import com.microsoft.z3.coverage.*
import com.sokolov.smt.Status
import com.sokolov.smt.isCertainBool
import com.sokolov.smt.isNot
import com.sokolov.smt.not
import com.sokolov.smt.prover.IProver
import com.sokolov.smt.sampler.logger
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.Model
import org.sosy_lab.java_smt.api.SolverContext

class ModelsIntersectionCoverage(
    context: SolverContext,
    prover: IProver,
    coveragePredicates: Collection<BooleanFormula>,
    val intersectionSize: Int = 3,
    private val nonChangedCoverageIterationsLimit: Int = 1
) : CoverageSampler(context, prover, coveragePredicates) {

    private var nonChangedCoverageIterations = 0

    private var uselessIntersectionModelsCoverage = 0
    private var usefulIntersectionModelsCoverage = 0


    override fun computeCoverage(
        coverModel: (Model) -> Set<AtomCoverageBase>,
        coverAtom: (assignment: Assignment<BooleanFormula>) -> AtomCoverageBase,
        onImpossibleAssignmentFound: (assignment: Assignment<BooleanFormula>) -> Unit
    ) {
        do {
            if (nonChangedCoverageIterations > 0)
                logger().trace("Non-changed coverage iterations: $nonChangedCoverageIterations")
            var coverageChanged = false

            val assertion: Assertion

            // if coverage not changed, let's try to add new assert
            if (nonChangedCoverageIterations >= nonChangedCoverageIterationsLimit) {
                logger().debug("Jump to another atoms in the formula due to $nonChangedCoverageIterationsLimit times coverage didn't change")
                val semiUncoveredAtoms = atomsWithSingleUncoveredValue

                logger().trace("atoms with single uncovered value: ${semiUncoveredAtoms.size}")

                if (semiUncoveredAtoms.isEmpty()) {
                    nonChangedCoverageIterations = 0
                    continue
                }

                val rememberedEnabledAssertions = customAssertionsStorage.assertions
                    .filter { !it.isLocal && it.enabled }
                    .onEach(Assertion::disable)

                val semiUncoveredAtomsAsExpr = semiUncoveredAtoms.mergeWithOr(formulaManager.booleanFormulaManager)

                assertion = customAssertionsStorage.assert(semiUncoveredAtomsAsExpr, false)

                when (checkWithAssumptions(checkStatusId())) {
                    Status.SAT -> {
                        val modelCoverage = coverModel(prover.model)
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
                assertion.disable()

                rememberedEnabledAssertions.forEach(Assertion::enable)

            } else {
                logger().debug("Extract intersections of atoms in $intersectionSize models")
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

                if (currentBoundModels.count() < intersectionSize)
                    break

                val intersection = currentBoundModels
                    .fold(
                        // TODO: keep in mind that eval must generate incomplete models
                        coveragePredicates.map { it to currentBoundModels.first().eval(it) }
                            .mapNotNull { if (it.second == null) null else Assignment(it.first, it.second!!) }
                            .toSet()
                    ) { acc, currentModel ->
                        // TODO: keep in mind that eval must generate incomplete models
                        acc.intersect(coveragePredicates.map { it to currentModel.eval(it) }
                            .mapNotNull { if (it.second == null) null else Assignment(it.first, it.second!!) }.toSet())
                    }.filter { it.value.isCertainBool(formulaManager.booleanFormulaManager) }

                logger().trace("intersection found: $intersection")


                if (intersection.isEmpty()) {
                    logger().info("No intersection found")
                    continue
                }
                logger().info("Found non-empty intersection consisting of ${intersection.size} atoms")

                val intersectionConstraint = intersection.mergeWithAnd(formulaManager.booleanFormulaManager)

                val negatedIntersection = intersectionConstraint.not(formulaManager.booleanFormulaManager)
                logger().trace("Add constraint on negated intersection")
                val negIntersectionAssertion = customAssertionsStorage.assert(negatedIntersection, false)

                assertion = negIntersectionAssertion

                if (checkWithAssumptions(checkStatusId()) == Status.UNSAT) {
                    // conflicted intersection found
                    resolveConflict(assertion, onImpossibleAssignmentFound)
                }
            }

            if (coverageChanged) nonChangedCoverageIterations = 0 else nonChangedCoverageIterations++
        } while (!isCovered)

        logger().info("Traversed ${modelsEnumerator.traversedModelsCount} models")
        logger().info("Useful models coverage from intersection: $usefulIntersectionModelsCoverage models")
        logger().info("Useless models coverage from intersection: $uselessIntersectionModelsCoverage models")
        logger().info("Useful / useless coeff of models coverage from intersection: ${usefulIntersectionModelsCoverage.toDouble() / uselessIntersectionModelsCoverage}")
    }

    /**
     * @return true if coverage changed
     */
    private fun resolveConflict(
        assertion: Assertion,
        onImpossibleAssignmentFound: (assignment: Assignment<BooleanFormula>) -> Unit,
    ) {
        logger().trace("Resolve conflict with $assertion")
        val unsatCore = unsatCoreWithAssumptions
        logger().trace("UnsatCore: $unsatCore")

        val customAssertionsFromCore = customAssertionsStorage.assertions.filter { it.uidExpr in unsatCore }

        val customUids = customAssertionsFromCore.map { it.uidExpr }
        val customConditions = customAssertionsFromCore.map { it.assumption }

        // if only custom assertions conflict
        if (customUids.size != 1 && unsatCore.all { it in customUids || it in customConditions }) {
            logger().info("Assertions conflict: custom assertions")
            (customAssertionsFromCore - assertion).filter { !it.isLocal }.forEach(Assertion::disable)
        } else {
            // conflict with original assertions branch
            logger().info("Assertions conflict: with original formula. Disable the assertion")
            assertion.disable()
            logger().trace("The assertion disabled: $assertion")

            if (customUids.size == 1) {
                logger().trace("Found unachievable value of atom: $assertion")

                val (atom, value) = if (formulaManager.booleanFormulaManager.isNot(assertion.expr))
                    assertion.expr.not(formulaManager.booleanFormulaManager) to formulaManager.booleanFormulaManager.makeFalse()
                else assertion.expr to formulaManager.booleanFormulaManager.makeTrue()
                onImpossibleAssignmentFound(Assignment(atom, value))
            }
        }
    }
}