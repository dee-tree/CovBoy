package com.sokolov.covboy.predicates

import org.ksmt.expr.KExpr
import org.ksmt.sort.KSort

interface PredicatesExtractor<E : KExpr<S>, S : KSort> {
    val predicates: Set<E>

    fun extractPredicates(expr: KExpr<*>): Set<E>

    fun extractPredicates(exprs: List<KExpr<*>>): Set<E>
}
