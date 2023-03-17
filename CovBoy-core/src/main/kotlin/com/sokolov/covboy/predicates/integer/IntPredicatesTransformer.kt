package com.sokolov.covboy.predicates.integer

import org.ksmt.KContext
import org.ksmt.expr.KExpr
import org.ksmt.sort.KBoolSort
import org.ksmt.sort.KIntSort

/**
 * Transform Int predicates (consts) to bool predicates
 */
internal class IntPredicatesTransformer(
    private val ctx: KContext,
    private val intPredicates: Set<KExpr<KIntSort>>
) {

    fun transform(): Set<KExpr<KBoolSort>> = with(ctx) {
        buildSet {
            intPredicates.forEach { intPredicate ->
                this += intPredicate gt 0.expr
                this += intPredicate lt 0.expr
                this += intPredicate eq 0.expr
            }
        }
    }

}

fun KContext.transformIntPredicates(predicates: Set<KExpr<KIntSort>>) = IntPredicatesTransformer(this, predicates).transform()
