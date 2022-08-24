package com.microsoft.z3.coverage

import com.microsoft.z3.*
import com.sokolov.z3cov.logger

class ModelsIntersectionCoverage(val intersectionSize: Int = 3, solver: Solver, context: Context) :
    CoverageSampler(solver, context) {

    private val modelsEnumerator = ModelsEnumerator(
        solver = solver,
        context = context,
        assertionsStorage = customAssertionsStorage,
        check = ::checkWithAssumptions
    )

    private val atoms = solver.atoms

    override fun computeCoveringModels(): Collection<Model> {
        val models = mutableSetOf<Model>()

        do {
            val currentBoundModels = modelsEnumerator.take(intersectionSize)
            models.addAll(currentBoundModels)
            if (currentBoundModels.count() < intersectionSize) break
            val intersection = currentBoundModels
                .fold(
                    atoms.map { it to currentBoundModels.first().eval(it, true) }.toSet()
                ) { acc, currentModel ->
                    acc.intersect(atoms.map { it to currentModel.eval(it, true) }.toSet())
                }

//            logger().info("intersection found: ${intersection}")

            if (intersection.isEmpty()) {
                logger().info("Found intersection, but it is empty")
                continue
            }
            logger().info("Found non-empty intersection consisting of ${intersection.size} atoms")


            // try to negate intersection constraint
            val rememberEnabledAssertions = buildMap {
                customAssertionsStorage.forEach {
                    put(it, it.enabled)
                    it.enabled = false
                }
            }
            val intersectionConstraint = intersection.connectWithAnd(context)
            logger().debug("Add constraint on negated intersection: ${!intersectionConstraint}")
            val assertion = customAssertionsStorage.assert(!intersectionConstraint, false)

//            if (checkWithAssumptions() == Status.UNSATISFIABLE) {
//                logger().info("UNSAT on asserting negated intersection")
//                assertion.enabled = false
//
//                customAssertionsStorage.assert(intersectionConstraint, false)
//                logger().info("Assert intersection to avoid get unsat on future assert the negated intersection")
//            }
            rememberEnabledAssertions.forEach { (rememberedAssertion, enabled) ->
                rememberedAssertion.enabled = enabled
            }
            // ---

            // check assertions conflict
            while (checkWithAssumptions() == Status.UNSATISFIABLE) {
                logger().info("Assertions conflict found")
                val unsatCore = solver.unsatCore
                logger().debug("UnsatCore: ${unsatCore.contentToString()}")
                val uids = unsatCore.filter { expr -> expr in customAssertionsStorage.assertions.map { it.uidExpr } }

                val assertionToBeDisabled = customAssertionsStorage.assertions
                    .firstOrNull { it.isLocal && it.uidExpr in uids } ?: break
                assertionToBeDisabled.enabled = false
                logger().debug("Disabled assertion: $assertionToBeDisabled")
            }

        } while (true)

        return models
    }
}