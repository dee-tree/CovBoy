package com.sokolov.covboy.coverage

import com.sokolov.covboy.coverage.predicate.CoveragePredicate
import org.ksmt.expr.KExpr
import org.ksmt.sort.KSort

data class FormulaCoverage<E : KExpr<T>, T : KSort>(
    val predicates: Set<CoveragePredicate<E, T>>
) : Comparable<FormulaCoverage<E, T>> {

    fun isEmpty(): Boolean = predicates.all { it.isEmpty }

    override fun compareTo(other: FormulaCoverage<E, T>): Int = compareValuesBy(this, other,
        { it.coveredValuesCount },
        { it.coveredPredicatesCount }
    )

    fun predicateByExpr(expr: E): CoveragePredicate<E, T> = predicates.first { it.expr == expr }

    val coveredPredicatesCount = predicates.count { it.isCovered }

    val coveredValuesCount = predicates.


    sumOf { it.satValues.size }

    operator fun minus(other: FormulaCoverage<E, T>): FormulaCoverage<E, T> = FormulaCoverage(
        predicates.map { it - other.predicateByExpr(it.expr) }.filter { !it.isEmpty }.toSet()
    )

}

fun <E: KExpr<T>, T: KSort> Collection<CoveragePredicate<E, T>>.toCoverage(): FormulaCoverage<E, T> = FormulaCoverage(this.toSet())