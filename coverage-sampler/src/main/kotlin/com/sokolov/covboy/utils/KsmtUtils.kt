package com.sokolov.covboy.utils

import org.ksmt.KContext
import org.ksmt.expr.KExpr
import org.ksmt.solver.KModel
import org.ksmt.solver.KSolver
import org.ksmt.solver.bitwuzla.KBitwuzlaSolver
import org.ksmt.solver.z3.KZ3Solver
import org.ksmt.sort.KBoolSort
import org.ksmt.sort.KSort
import kotlin.reflect.KClass

typealias KBoolExpr = KExpr<KBoolSort>

fun <T : KSort> KModel.evalOrNull(expr: KExpr<T>): KExpr<T>? = eval(expr).let {
    if (it == expr) null
    else it
}

val KClass<out KSolver<*>>.solverName: String
    get() = when (simpleName) {
        KZ3Solver::class.simpleName -> "Z3"
        KBitwuzlaSolver::class.simpleName -> "Bitwuzla"
        else -> this::class.simpleName!!
    }

val KSolver<*>.solverName: String
    get() = this::class.solverName

fun solverBuilder(solverName: String): (KContext) -> KSolver<*> = when(solverName) {
    KZ3Solver::class.solverName -> { ctx: KContext -> KZ3Solver(ctx) }
    KBitwuzlaSolver::class.solverName -> { ctx: KContext -> KBitwuzlaSolver(ctx) }
    else -> throw IllegalArgumentException("Solver with name $solverName not found")
}
