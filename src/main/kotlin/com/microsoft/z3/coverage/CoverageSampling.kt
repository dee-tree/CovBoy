package com.microsoft.z3.coverage

import com.microsoft.z3.*

class CoverageSampling(
    private val solver: Solver,
    private val context: Context,
) {

    private val atoms = solver.atoms
    private val customAssertions = AssertionsStorage(solver, context)
    private val modelsEnumerator = ModelsEnumerator(solver, context, customAssertions)


    fun enumerate() {
        do {
            val currentBoundModels = modelsEnumerator.take(3)
            if (currentBoundModels.count() < 3) break
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
                customAssertions.forEach {
                    put(it, it.enabled)
                    it.enabled = false
                }
            }
            val intersectionConstraint = intersection.connectWithAnd(context)
            println("Assert !intersection: ${!intersectionConstraint}")
            val assertion = customAssertions.assert(!intersectionConstraint)

            if (checkWithAssumptions() == Status.UNSATISFIABLE) {
                println("Essential constraint for satisfiability: ${intersectionConstraint}")
                assertion.enabled = false

                customAssertions.assert(intersectionConstraint)
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
                val uids = unsatCore.filter { ucExpr -> ucExpr in customAssertions.assertions.map { it.uidExpr } }

                val assertionToBeDisabled = customAssertions.assertions.first { it.uidExpr in uids }
                assertionToBeDisabled.enabled = false
                println("Disabled assertion: $assertionToBeDisabled")
            }

        } while (true)
        println(solver.unsatCore.contentToString())
    }

    private fun checkWithAssumptions(): Status = solver.check(*customAssertions.assumptions.toTypedArray())

}

private class ModelsEnumerator(
    private val solver: Solver,
    private val context: Context,
    private val assertionsStorage: AssertionsStorage
) {
    private lateinit var current: Model

    private val atoms = solver.atoms
    private var count = 0
    fun hasNext(assumptions: Collection<Expr> = emptyList()): Boolean =
        solver.check(*assumptions.toTypedArray()) == Status.SATISFIABLE

    fun nextModel(assumptions: Collection<Expr> = emptyList()): Pair<Model, Assertion> {
        if (solver.check(*assumptions.toTypedArray()) != Status.SATISFIABLE) {
            throw IllegalStateException("UNSAT on attempt to enumerate models")
        }

        current = solver.model
        val currentConstraints = atoms.map { it to current.eval(it, true) }.connectWithAnd(context)
        println("Model found: $currentConstraints (${++count})")

        return current to assertionsStorage.assert(!currentConstraints).also { if (!it.enabled) it.enabled = true }
    }

    fun take(count: Int): List<Model> = buildList {
        repeat(count) {
            val assumptions = assertionsStorage.assumptions
            if (!hasNext(assumptions))
                return@buildList
            add(nextModel(assumptions).first)
        }
    }
}

private fun Collection<Pair<BoolExpr, Expr>>.connectWithAnd(context: Context): BoolExpr {
    with(context) {
        if (size == 1) return first().let { if (it.second == mkTrue()) it.first else !it.first }

        return and(
            *(filter { it.second == mkTrue() }.map { it.first }
                    + filter { it.second == mkFalse() }.map { !it.first })
                .toTypedArray()
        )
    }
}