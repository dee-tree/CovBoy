package com.microsoft.z3.coverage

import com.microsoft.z3.BoolExpr
import com.microsoft.z3.Context
import com.microsoft.z3.Solver
import com.microsoft.z3.implies

class Assertion(
    val expr: BoolExpr,
    context: Context,
    val isLocal: Boolean,
    var onAssertionChanged: ((AssertionState) -> Unit)? = null
) {
    val uid = "uid:${expr.hashCode()}"
    val uidExpr: BoolExpr = context.mkBoolConst(uid)

    val assumptionName = "cond:${expr.hashCode()}"
    val conditionExpr = context.mkBoolConst(assumptionName)

    val assumption: BoolExpr
        get() = conditionExpr

    var enabled = true
        private set

    fun put(solver: Solver): Assertion = apply {
        solver.assertAndTrack(assumption implies expr, uidExpr)
        onAssertionChanged?.invoke(AssertionState(uid, enabled))
    }

    fun disable() {
        enabled = false
        onAssertionChanged?.invoke(AssertionState(uid, enabled))
    }

    fun enable() {
        enabled = true
        onAssertionChanged?.invoke(AssertionState(uid, enabled))
    }

    override fun toString(): String = "AssertionInfo(uid = $uid, enabled = $enabled, expression hash = ${expr.hashCode()})"
}

data class AssertionState(
    val uid: String,
    val enabled: Boolean
)