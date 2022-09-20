package com.microsoft.z3.coverage

import com.microsoft.z3.*

class ModelsEnumerator(
    private val solver: Solver,
    private val context: Context,
    private val assertionsStorage: AssertionsStorage,
    private val check: () -> Status = solver::check
) {
    private lateinit var current: Model

    private val atoms = solver.atoms

    var traversedModelsCount = 0
        private set

    fun hasNext(): Boolean = check() == Status.SATISFIABLE

    fun nextModel(): Pair<Model, Assertion> {
        current = solver.model
        // get incomplete models to avoid "unknown"/"undefined" predicates
        val currentConstraints = atoms
            .map { it to current.eval(it, false) }
            .filter { it.second.isCertainBool }
            .mergeWithAnd(context)

        val result = current to assertionsStorage.assert(!currentConstraints, true)
            .also { if (!it.enabled) it.enable() }
        traversedModelsCount++

        return result
    }

    fun take(count: Int): List<Model> = buildList {
        if (count < 1) return@buildList

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

    fun forEach(action: (Model) -> Unit) {
        while (hasNext())
            action(nextModel().first)
    }
}

internal fun Collection<Pair<BoolExpr, Expr>>.mergeWith(merger: (Array<out BoolExpr>) -> BoolExpr): BoolExpr {
    if (size == 1) return first().let {
        when {
            it.second.isFalse -> !it.first
            else -> it.first
        }
    }

    return merger(
        (filter { it.second.isTrue }.map { it.first }
                + filter { it.second.isFalse }.map { !it.first }
                ).toTypedArray()
    )
}

internal fun Collection<Pair<BoolExpr, Expr>>.mergeWithAnd(context: Context): BoolExpr = mergeWith(context::and)
internal fun Collection<Pair<BoolExpr, Expr>>.mergeWithOr(context: Context): BoolExpr = mergeWith(context::or)

internal fun Map<BoolExpr, Expr>.mergeWithAnd(context: Context): BoolExpr = this.entries
    .map { it.key to it.value }
    .mergeWithAnd(context)

internal fun Map<BoolExpr, Expr>.mergeWithOr(context: Context): BoolExpr = this.entries
    .map { it.key to it.value }
    .mergeWithOr(context)