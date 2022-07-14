package com.microsoft.z3

fun Context.boolConst(name: String): BoolExpr = mkBoolConst(name)

fun Context.and(vararg boolExprs: BoolExpr): BoolExpr = mkAnd(*boolExprs)
infix fun BoolExpr.and(boolExpr: BoolExpr): BoolExpr = context.and(this, boolExpr)
fun BoolExpr.and(vararg boolExprs: BoolExpr): BoolExpr = context.and(this, *boolExprs)

fun Context.or(vararg boolExprs: BoolExpr): BoolExpr = mkOr(*boolExprs)
infix fun BoolExpr.or(boolExpr: BoolExpr): BoolExpr = context.or(this, boolExpr)
fun BoolExpr.or(vararg boolExprs: BoolExpr): BoolExpr = context.or(this, *boolExprs)

fun Context.xor(a: BoolExpr, b: BoolExpr): BoolExpr = mkXor(a, b)
infix fun BoolExpr.xor(boolExpr: BoolExpr): BoolExpr = context.xor(this, boolExpr)

fun Context.implies(a: BoolExpr, b: BoolExpr): BoolExpr = mkImplies(a, b)
infix fun BoolExpr.implies(boolExpr: BoolExpr): BoolExpr = context.implies(this, boolExpr)

fun Context.not(a: BoolExpr): BoolExpr = mkNot(a)
operator fun BoolExpr.not(): BoolExpr = context.not(this)

fun Context.eq(a: Expr, b: Expr): BoolExpr = mkEq(a, b)
infix fun Expr.eq(expr: Expr): BoolExpr = context.eq(this, expr)
