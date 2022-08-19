package com.microsoft.z3.coverage

import com.microsoft.z3.*

class ModelsIntersectionCoverage(val intersectionSize: Int = 3, solver: Solver, context: Context) : CoverageSampler(solver, context) {

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

            println("intersection: ${intersection}")

            if (intersection.isEmpty()) continue

            // try to negate intersection constraint
            val rememberEnabledAssertions = buildMap {
                customAssertionsStorage.forEach {
                    put(it, it.enabled)
                    it.enabled = false
                }
            }
            val intersectionConstraint = intersection.connectWithAnd(context)
            println("Assert !intersection: ${!intersectionConstraint}")
            val assertion = customAssertionsStorage.assert(!intersectionConstraint, false)

            if (checkWithAssumptions() == Status.UNSATISFIABLE) {
                println("Essential constraint for satisfiability: ${intersectionConstraint}")
                assertion.enabled = false

                customAssertionsStorage.assert(intersectionConstraint, false)
            }
            rememberEnabledAssertions.forEach { (rememberedAssertion, enabled) ->
                rememberedAssertion.enabled = enabled
            }
            // ---

            // check assertions conflict
            while (checkWithAssumptions() == Status.UNSATISFIABLE) {
                println("Assertions conflict found")
                val unsatCore = solver.unsatCore
                println("Unsat core: ${unsatCore.contentToString()}")
                val uids =
                    unsatCore.filter { ucExpr -> ucExpr in customAssertionsStorage.assertions.map { it.uidExpr } }

                val assertionToBeDisabled = customAssertionsStorage.assertions.firstOrNull { it.isLocal && it.uidExpr in uids } ?: break
                assertionToBeDisabled.enabled = false
                println("Disabled assertion: $assertionToBeDisabled")
            }

        } while (true)

        return models
    }
}