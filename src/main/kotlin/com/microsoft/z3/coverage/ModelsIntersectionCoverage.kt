package com.microsoft.z3.coverage

import com.microsoft.z3.*
import com.sokolov.z3cov.logger

class ModelsIntersectionCoverage(
    solver: Solver,
    context: Context,
    val intersectionSize: Int = 3,
    private val nonChangedCoverageIterationsLimit: Int = 1
) : CoverageSampler(solver, context) {

    private val atoms = solver.atoms

    private var nonChangedCoverageIterations = 0

    private var uselessIntersectionModelsCoverage = 0
    private var usefulIntersectionModelsCoverage = 0

    private var satCount = 0
    private var unsatCount = 0

    override fun computeCoverage(
        coverModel: (Model) -> Set<AtomCoverageBase>,
        coverAtom: (atom: BoolExpr, value: BoolExpr) -> AtomCoverageBase,
        onImpossibleAtomValueFound: (atom: BoolExpr, impossibleValue: BoolExpr) -> Unit
    ) {
        if (checkWithAssumptions() != Status.SATISFIABLE) {
            logger().warn("Formula is UNSAT initially")
            return
        }

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

                val semiUncoveredAtomsAsExpr = semiUncoveredAtoms.mergeWithOr(context)

                assertion = customAssertionsStorage.assert(semiUncoveredAtomsAsExpr, false)

                when (checkWithAssumptions()) {
                    Status.SATISFIABLE -> {
                        satCount++
                        val modelCoverage = coverModel(solver.model)
                        coverageChanged = modelCoverage.any { it !is EmptyAtomCoverage }
                    }
                    Status.UNSATISFIABLE -> {
                        unsatCount++
                        atomsWithSingleUncoveredValue.forEach(onImpossibleAtomValueFound)
                    }
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
                        atoms.map { it to currentBoundModels.first().eval(it, false) }.toSet()
                    ) { acc, currentModel ->
                        acc.intersect(atoms.map { it to currentModel.eval(it, false) }.toSet())
                    }.filter { it.second.isCertainBool }

                logger().trace("intersection found: $intersection")


                if (intersection.isEmpty()) {
                    logger().info("No intersection found")
                    continue
                }
                logger().info("Found non-empty intersection consisting of ${intersection.size} atoms")

                val intersectionConstraint = intersection.mergeWithAnd(context)

                val negatedIntersection = !intersectionConstraint
                logger().trace("Add constraint on negated intersection")
                val negIntersectionAssertion = customAssertionsStorage.assert(negatedIntersection, false)

                assertion = negIntersectionAssertion

                when (checkWithAssumptions()) {
                    Status.SATISFIABLE -> {
                        satCount++
                    }
                    Status.UNSATISFIABLE -> {
                        unsatCount++

                        // conflicted intersection found
                        resolveConflict(assertion, onImpossibleAtomValueFound)
                    }
                }
            }

            if (coverageChanged) nonChangedCoverageIterations = 0 else nonChangedCoverageIterations++
        } while (!isCovered)

        logger().info("Traversed ${modelsEnumerator.traversedModelsCount} models")
        logger().info("Useful models coverage from intersection: $usefulIntersectionModelsCoverage models")
        logger().info("Useless models coverage from intersection: $uselessIntersectionModelsCoverage models")
        logger().info("Useful / useless coeff of models coverage from intersection: ${usefulIntersectionModelsCoverage.toDouble() / uselessIntersectionModelsCoverage}")
        logger().debug("SATs: $satCount (${satCount.toDouble() / (satCount + unsatCount)}); UNSATs: $unsatCount (${unsatCount.toDouble() / (satCount + unsatCount)})")
    }

    /**
     * @return true if coverage changed
     */
    private fun resolveConflict(
        assertion: Assertion,
        onImpossibleAtomValueFound: (atom: BoolExpr, impossibleValue: BoolExpr) -> Unit,
    ) {
        logger().trace("Resolve conflict with $assertion")
        val unsatCore = solver.unsatCore
        logger().trace("UnsatCore: ${unsatCore.contentToString()}")

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

                val (atom, value) = if (assertion.expr.isNot) !assertion.expr to context.mkFalse() else assertion.expr to context.mkTrue()
                onImpossibleAtomValueFound(atom, value)
            }
        }
    }
}