package com.sokolov.covboy.utils

import org.ksmt.expr.KExpr
import org.ksmt.solver.KModel
import org.ksmt.solver.KSolver
import org.ksmt.solver.bitwuzla.KBitwuzlaSolver
import org.ksmt.solver.z3.KZ3Solver
import org.ksmt.sort.KBoolSort
import org.ksmt.sort.KSort

typealias KBoolExpr = KExpr<KBoolSort>

fun <T : KSort> KModel.evalOrNull(expr: KExpr<T>): KExpr<T>? = eval(expr).let {
    if (it == expr) null
    else it
}

val KSolver<*>.solverName: String
    get() = when (this) {
        KZ3Solver::class -> "Z3"
        KBitwuzlaSolver::class -> "Bitwuzla"
        else -> this::class.simpleName!!
    }