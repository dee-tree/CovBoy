package com.sokolov.covboy

import org.ksmt.expr.KExpr
import org.ksmt.sort.KSort
import java.io.OutputStream

class PredicatesCoverage<S : KSort>(
    val coverageSat: Map<KExpr<S>, Set<KExpr<S>>>,
    val coverageUnsat: Map<KExpr<S>, Set<KExpr<S>>>,
    val coverageUniverse: Set<KExpr<S>>
) {

    fun isCovered(expr: KExpr<S>): Boolean = expr.isCovered(
        coverageSat.getValue(expr),
        coverageUnsat.getValue(expr),
        coverageUniverse
    )

    fun serialize(out: OutputStream) {
        // TODO
    }
}