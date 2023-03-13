package com.sokolov.covboy

import org.ksmt.expr.KExpr
import org.ksmt.solver.KSolver
import org.ksmt.solver.KSolverStatus
import org.ksmt.sort.KSort

fun KSolver<*>.ensureSat(msg: () -> String) {
    this.check().also { status ->
        if (status != KSolverStatus.SAT) {
            throw IllegalStateException("Formula is $status, but expected: ${KSolverStatus.SAT}. ${msg()}")
        }
    }
}


fun <S: KSort> KExpr<S>.isCovered(
    coverageSat: Set<KExpr<S>>,
    coverageUnsat: Set<KExpr<S>>,
    universe: Set<KExpr<S>>
): Boolean = (coverageSat + coverageUnsat).containsAll(universe)

