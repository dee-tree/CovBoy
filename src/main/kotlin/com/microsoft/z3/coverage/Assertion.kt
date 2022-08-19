package com.microsoft.z3.coverage

import com.microsoft.z3.BoolExpr
import com.microsoft.z3.Context
import com.microsoft.z3.Solver
import com.microsoft.z3.implies

class Assertion(val expr: BoolExpr, context: Context) {
    val uid = "uid:${expr.hashCode()}"
    val uidExpr: BoolExpr = context.mkBoolConst(uid)

    var enabled = true

    val assumptionName = "cond:${expr.hashCode()}"
    val conditionExpr = context.mkBoolConst(assumptionName)

    val assumption: BoolExpr
        get() = conditionExpr

    fun put(solver: Solver): Assertion = apply { solver.assertAndTrack(assumption implies expr, uidExpr) }

    override fun toString(): String = "AssertionInfo(uid = $uid, enabled = $enabled, expr = $expr)"
}