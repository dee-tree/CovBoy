package com.microsoft.z3.coverage

import com.microsoft.z3.*

internal class ModelsEnumerator(
    private val solver: Solver,
    private val context: Context,
    private val assertionsStorage: AssertionsStorage,
    private val check: () -> Status = solver::check
) {
    private lateinit var current: Model

    private val atoms = solver.atoms
    private var count = 0

    fun hasNext(): Boolean = check() == Status.SATISFIABLE

    fun nextModel(): Pair<Model, Assertion> {
        if (check() != Status.SATISFIABLE) {
            throw IllegalStateException("UNSAT on attempt to enumerate models")
        }

        current = solver.model
        val currentConstraints = atoms.map { it to current.eval(it, true) }.connectWithAnd(context)
        println("Model found: $currentConstraints (${++count})")

        return current to assertionsStorage.assert(!currentConstraints, true).also { if (!it.enabled) it.enabled = true }
    }

    fun take(count: Int): List<Model> = buildList {
        repeat(count) {
            if (!hasNext())
                return@buildList
            add(nextModel().first)
        }
    }

    fun all(): List<Model> = buildList {
        while (hasNext())
            add(nextModel().first)
    }
}

internal fun Collection<Pair<BoolExpr, Expr>>.connectWithAnd(context: Context): BoolExpr {
    with(context) {
        if (size == 1) return first().let { if (it.second == mkTrue()) it.first else !it.first }

        return and(
            *(filter { it.second == mkTrue() }.map { it.first }
                    + filter { it.second == mkFalse() }.map { !it.first })
                .toTypedArray()
        )
    }
}