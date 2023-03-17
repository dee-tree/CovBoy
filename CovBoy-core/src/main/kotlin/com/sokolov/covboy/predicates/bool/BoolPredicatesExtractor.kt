package com.sokolov.covboy.predicates.bool

import com.sokolov.covboy.predicates.PredicatesExtractor
import org.ksmt.KContext
import org.ksmt.expr.*
import org.ksmt.expr.transformer.KTransformer
import org.ksmt.sort.KBoolSort
import org.ksmt.sort.KSort
import org.ksmt.utils.uncheckedCast

class BoolPredicatesExtractor(override val ctx: KContext) : KTransformer, PredicatesExtractor<KExpr<KBoolSort>, KBoolSort> {

    val bools = HashSet<KExpr<KBoolSort>>()

    override fun extractPredicates(expr: KExpr<*>): Set<KExpr<KBoolSort>> {
        expr.accept(this)
        return predicates
    }

    override fun extractPredicates(exprs: List<KExpr<*>>): Set<KExpr<KBoolSort>> {
        exprs.forEach { it.accept(this) }
        return predicates
    }

    override val predicates: Set<KExpr<KBoolSort>>
        get() = bools

    override fun <T : KSort, A : KSort> transformApp(expr: KApp<T, A>): KExpr<T> {
        if (expr.sort is KBoolSort && expr !is KTrue && expr !is KFalse) {
            if (expr.args.any { it.sort is KBoolSort }) {
                expr.args.forEach { if (it.sort is KBoolSort) it.accept(this) }
            } else {
                bools.add(expr.uncheckedCast())
            }
        }
        return expr
    }

    override fun <T : KSort> transform(expr: KConst<T>): KExpr<T> {

        if (expr.sort is KBoolSort) {
            bools.add(expr.uncheckedCast())
        }

        return expr
    }

    private fun transformQuantifier(expr: KQuantifier): KExpr<KBoolSort> {
        val bounds = BoolPredicatesExtractor(ctx).extractPredicates(expr.bounds.map { it.apply(emptyList()) })
        val bodyPredicates = BoolPredicatesExtractor(ctx).extractPredicates(expr.body)

        bools.addAll(bodyPredicates - bounds)
        return expr
    }

    override fun transform(expr: KUniversalQuantifier): KExpr<KBoolSort> = transformQuantifier(expr)

    override fun transform(expr: KExistentialQuantifier): KExpr<KBoolSort> = transformQuantifier(expr)
}
