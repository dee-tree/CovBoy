package com.sokolov.covboy.coverage.predicate

import org.ksmt.expr.KExpr
import org.ksmt.sort.KSort

interface PredicatesExtractor<E : KExpr<T>, T : KSort> {
    val predicates: Set<CoveragePredicate<E, T>>

    fun extractPredicates(expr: KExpr<*>): Set<CoveragePredicate<E, T>>

    fun extractPredicates(exprs: List<KExpr<*>>): Set<CoveragePredicate<E, T>>
}