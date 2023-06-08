package com.sokolov.covboy.coverage

import com.sokolov.covboy.isCovered
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

    fun equalsCoverage(other: PredicatesCoverage<S>): Boolean {
        if (coverageSat != other.coverageSat) {
            if (coverageSat.any { (predicate, values) ->
                    values.any { it in (other.coverageUnsat[predicate] ?: emptySet()) }
                }) return false

            if (other.coverageSat.any { (predicate, values) ->
                    values.any { it in (coverageUnsat[predicate] ?: emptySet()) }
                }) return false
        }
        if (coverageUnsat != other.coverageUnsat) {
            if (coverageUnsat.any { (predicate, values) ->
                    values.any { it in (other.coverageSat[predicate] ?: emptySet()) }
                }) return false

            if (other.coverageUnsat.any { (predicate, values) ->
                    values.any { it in (coverageSat[predicate] ?: emptySet()) }
                }) return false
        }

        return true
    }

    fun serialize(ctx: KContext, out: OutputStream) = with(PredicatesCoverageSerializer(ctx)) {
        this@PredicatesCoverage.serialize(out)
    }

    companion object {
        fun <S : KSort> deserialize(ctx: KContext, input: InputStream): PredicatesCoverage<S> =
            PredicatesCoverageSerializer(ctx).deserialize(input)
    }
}
