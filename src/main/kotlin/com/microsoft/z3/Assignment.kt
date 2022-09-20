package com.microsoft.z3

class Assignment <T: Expr>(val expr: T, val value: T) {

    fun asExpr(): BoolExpr = when {
        expr is BoolExpr && value.isFalse -> !expr
        expr is BoolExpr && value.isTrue -> expr
        else -> expr eq value
    }

    override fun toString(): String = "AssignedExpr(expr = $expr, value = $value)"
}

