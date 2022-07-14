package com.microsoft.z3

fun Context.solver() = mkSolver()
fun Context.optimize() = mkOptimize()

fun Optimize.add(vararg boolExprs: BoolExpr) = Add(*boolExprs)
fun Optimize.check(): Status = Check()
fun Optimize.push() = Push()
fun Optimize.pop() = Pop()

fun Solver.whileSat(action: (Model) -> BoolExpr) {
    while (check() == Status.SATISFIABLE) {
        add(action(model))
    }
}

fun Optimize.whileSat(action: (Model) -> BoolExpr) {
    while (check() == Status.SATISFIABLE) {
        add(action(model))
    }
}