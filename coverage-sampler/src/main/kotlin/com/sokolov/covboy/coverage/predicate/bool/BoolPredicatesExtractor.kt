package com.sokolov.covboy.coverage.predicate.bool

import com.sokolov.covboy.coverage.predicate.CoveragePredicate
import com.sokolov.covboy.coverage.predicate.PredicatesExtractor
import com.sokolov.covboy.utils.KBoolExpr
import org.ksmt.KContext
import org.ksmt.expr.*
import org.ksmt.expr.transformer.KTransformer
import org.ksmt.sort.KBoolSort
import org.ksmt.sort.KSort

class BoolPredicatesExtractor(override val ctx: KContext) : KTransformer, PredicatesExtractor<KBoolExpr, KBoolSort> {

    val bools = mutableSetOf<KBoolExpr>()

    override fun extractPredicates(expr: KExpr<*>): Set<CoveragePredicate<KBoolExpr, KBoolSort>> {
        expr.accept(this)
        return predicates
    }

    override fun extractPredicates(exprs: List<KExpr<*>>): Set<CoveragePredicate<KBoolExpr, KBoolSort>> {
        exprs.forEach { it.accept(this) }
        return predicates
    }

    override val predicates: Set<CoveragePredicate<KBoolExpr, KBoolSort>>
        get() = bools.map { CoverageBoolPredicate(it, ctx) }.toSet()

    override fun <T : KSort> transformApp(expr: KApp<T, *>): KExpr<T> {
        if (expr.sort is KBoolSort && expr !is KTrue && expr !is KFalse) {
            if (expr.args.any { it.sort is KBoolSort }) {
                expr.args.forEach { if (it.sort is KBoolSort) it.accept(this) }
            } else {
                bools += expr as KExpr<KBoolSort>
            }
        }
        return expr
    }

    override fun <T : KSort> transform(expr: KConst<T>): KExpr<T> {

        if (expr.sort is KBoolSort) {
            bools += expr as KConst<KBoolSort>
        }

        return expr
    }
}
