package com.microsoft.z3.coverage

import com.microsoft.z3.*
import com.sokolov.z3cov.logger

class ModelsIntersectionCoverage(
    solver: Solver,
    context: Context,
    val intersectionSize: Int = 3,
    private val immutableCoverageIterationsLimit: Int = 1
) : CoverageSampler(solver, context) {


    private val modelsEnumerator = ModelsEnumerator(
        solver = solver,
        context = context,
        assertionsStorage = customAssertionsStorage,
        check = ::checkWithAssumptions
    )

    private val atoms = solver.atoms

    private var nonChangedCoverageIterations = 0

    override fun computeCoverage(
        coverModel: (Model) -> Set<AtomCoverageBase>,
        coverAtom: (atom: BoolExpr, value: BoolExpr) -> AtomCoverageBase
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
            if (nonChangedCoverageIterations > immutableCoverageIterationsLimit) {
                logger().debug("Jump to another atoms in the formula due to $immutableCoverageIterationsLimit times coverage immutability")
                val uncoveredAtom = firstSemiCoveredAtom

                logger().debug("first semi-uncovered atom: $uncoveredAtom")

                if (uncoveredAtom == null) {
                    nonChangedCoverageIterations = 0
                    continue
                }

                val uncoveredAtomAsExpr =
                    if (uncoveredAtom.second == context.mkTrue()) uncoveredAtom.first else !uncoveredAtom.first
                assertion = customAssertionsStorage.assert(uncoveredAtomAsExpr, false)

                nonChangedCoverageIterations = 0
            } else {
                logger().debug("Extract intersections of atoms in $intersectionSize models")
                val currentBoundModels = modelsEnumerator.take(intersectionSize)

                currentBoundModels.forEach {
                    val currentModelCoverage = coverModel(it)

                    coverageChanged = coverageChanged || currentModelCoverage.any { atomCoverage -> atomCoverage !is EmptyAtomCoverage }
                }

                if (currentBoundModels.count() < intersectionSize)
                    break

                val intersection = currentBoundModels
                    .fold(
                        atoms.map { it to currentBoundModels.first().eval(it, false) }.toSet()
                    ) { acc, currentModel ->
                        acc.intersect(atoms.map { it to currentModel.eval(it, false) }.toSet())
                    }.filter { it.second.isCertainBool }

                logger().trace("intersection found: ${intersection}")


                if (intersection.isEmpty()) {
                    logger().info("No intersection found")
                    continue
                }
                logger().info("Found non-empty intersection consisting of ${intersection.size} atoms")

                val intersectionConstraint = intersection.connectWithAnd(context)

                val negatedIntersection = !intersectionConstraint
//                logger().trace("Add constraint on negated intersection: $negatedIntersection")
                logger().trace("Add constraint on negated intersection")
                val negIntersectionAssertion = customAssertionsStorage.assert(negatedIntersection, false)

                assertion = negIntersectionAssertion
            }

            val checkStatus = checkWithAssumptions()
            // check assertions conflict
            if (checkStatus == Status.UNSATISFIABLE) {
                val coverageChangedAfterResolution = resolveConflict(assertion, coverAtom)
                logger().info("Coverage ${if (coverageChangedAfterResolution) "did" else "didn't"} change after conflict resolution")
                coverageChanged = coverageChanged || coverageChangedAfterResolution
            }

            if (coverageChanged) nonChangedCoverageIterations = 0 else nonChangedCoverageIterations++
        } while (!isCovered)

        logger().info("Traversed ${modelsEnumerator.traversedModelsCount} models")
    }

    /**
     * @return true if coverage changed
     */
    private fun resolveConflict(
        assertion: Assertion,
        coverAtom: (atom: BoolExpr, value: BoolExpr) -> AtomCoverageBase
    ): Boolean {
        logger().trace("Resolve conflict with $assertion")
        val unsatCore = solver.unsatCore
        logger().trace("UnsatCore: ${unsatCore.contentToString()}")

        val customAssertionsFromCore = customAssertionsStorage.assertions.filter { it.uidExpr in unsatCore }

//        logger().trace("custom assertions in unsat core: $customAssertionsFromCore")

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
            logger().trace("The assertion disabled successfully: $assertion")

            if (customUids.size == 1) {
                logger().info("Cover impossible value of atom!")
                logger().trace("Cover impossible value of atom $assertion")

                val (atom, value) = if (assertion.expr.isNot) !assertion.expr to context.mkFalse() else assertion.expr to context.mkTrue()
                return coverAtom(atom, value) !is EmptyAtomCoverage
            }
        }
        return false
    }
}