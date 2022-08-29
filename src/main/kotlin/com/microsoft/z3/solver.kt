package com.microsoft.z3

fun Context.solver(
    partialModels: Boolean? = null,
    randomSeed: Int? = null
): Solver {
    val solver = mkSolver()
    val params = mkParams()

    partialModels?.let { optionEnabled ->
        params.add("model.partial", optionEnabled)
    }
    randomSeed?.let { seed ->
        params.add("random_seed", seed)
    }

    solver.setParameters(params)

    return solver
}

fun Context.optimize() = mkOptimize()

fun Optimize.add(vararg boolExprs: BoolExpr) = Add(*boolExprs)
fun Optimize.check(): Status = Check()
fun Optimize.push() = Push()
fun Optimize.pop() = Pop()

fun Solver.whileSat(action: (Model) -> Unit) {
    while (check() == Status.SATISFIABLE) {
        action(model)
    }
}

fun Optimize.whileSat(action: (Model) -> BoolExpr) {
    while (check() == Status.SATISFIABLE) {
        add(action(model))
    }
}