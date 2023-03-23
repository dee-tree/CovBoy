package com.sokolov.covboy

import org.ksmt.KContext
import org.ksmt.expr.KExpr
import org.ksmt.runner.generated.models.SolverType
import org.ksmt.sort.KSort
import java.io.InputStream
import java.io.OutputStream

data class PredicatesCoverage<S : KSort>(
    val coverageSat: Map<KExpr<S>, Set<KExpr<S>>>,
    val coverageUnsat: Map<KExpr<S>, Set<KExpr<S>>>,
    val coverageUniverse: Set<KExpr<S>>,
    val solverType: SolverType
) {

    fun isCovered(expr: KExpr<S>): Boolean = expr.isCovered(
        coverageSat.getValue(expr),
        coverageUnsat.getValue(expr),
        coverageUniverse
    )

    fun serialize(ctx: KContext, out: OutputStream) = with(PredicatesCoverageSerializer(ctx)) {
        this@PredicatesCoverage.serialize(out)
    }

    companion object {
        fun <S : KSort> deserialize(ctx: KContext, input: InputStream): PredicatesCoverage<S> =
            PredicatesCoverageSerializer(ctx).deserialize(input)
    }
}
