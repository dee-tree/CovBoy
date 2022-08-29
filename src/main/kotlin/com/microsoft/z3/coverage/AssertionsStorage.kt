package com.microsoft.z3.coverage

import com.microsoft.z3.BoolExpr
import com.microsoft.z3.Context
import com.microsoft.z3.Solver

class AssertionsStorage(
    private val solver: Solver,
    private val context: Context,
    var onAssertionChanged: ((newState: AssertionState) -> Unit)? = null,
    vararg initial
    : Assertion,
) {

    private val storage = mutableListOf<Assertion>(*initial)

    val size: Int
        get() = storage.size

    val assumptions: List<BoolExpr>
        get() = storage.mapNotNull { if (it.enabled) it.assumption else null }

    private fun assertSafely(expr: BoolExpr, isLocal: Boolean): Assertion {
        val assertion = Assertion(expr, context, isLocal) { onAssertionChanged?.invoke(it) }
        storage.find { it.uid == assertion.uid }?.let { return it }
        return assertion.put(solver).also { storage.add(it) }
    }

    fun assert(expr: BoolExpr, isLocal: Boolean): Assertion {
        return assertSafely(expr, isLocal)
    }

    fun forEach(action: (Assertion) -> Unit) {
        storage.forEach(action)
    }

    val assertions: Collection<Assertion>
        get() = storage

    operator fun get(uid: String): Assertion = storage.first { it.uid == uid }

    fun enabled(uid: String): Boolean = storage.first { it.uid == uid }.enabled
}