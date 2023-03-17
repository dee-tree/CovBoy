package com.sokolov.covboy.predicates.integer

import com.sokolov.covboy.predicates.PredicatesExtractor
import org.ksmt.KContext
import org.ksmt.expr.*
import org.ksmt.expr.transformer.KTransformer
import org.ksmt.sort.KBoolSort
import org.ksmt.sort.KIntSort
import org.ksmt.sort.KSort
import org.ksmt.utils.uncheckedCast

class IntPredicatesExtractor(override val ctx: KContext) : KTransformer, PredicatesExtractor<KExpr<KIntSort>, KIntSort> {

    val ints = hashSetOf<KExpr<KIntSort>>()

    override val predicates: Set<KExpr<KIntSort>>
        get() = ints


    override fun extractPredicates(expr: KExpr<*>): Set<KExpr<KIntSort>> {
        expr.accept(this)
        return predicates
    }

    override fun extractPredicates(exprs: List<KExpr<*>>): Set<KExpr<KIntSort>> {
        exprs.forEach { it.accept(this) }
        return predicates
    }

    override fun <T : KSort, A : KSort> transformApp(expr: KApp<T, A>): KExpr<T> {
        if (expr.sort is KIntSort && expr !is KIntNumExpr) {
            if (expr.args.any { it.sort is KIntSort }) {
                expr.args.forEach { if (it.sort is KIntSort) it.accept(this) }
            } else {
                ints.add(expr.uncheckedCast())
            }
        } else {
            expr.args.forEach { it.accept(this) }
        }
        return expr
    }

    override fun <T : KSort> transform(expr: KConst<T>): KExpr<T> {
        if (expr.sort is KIntSort) {
            ints.add(expr.uncheckedCast())
        }

        return expr
    }

    private fun transformQuantifier(expr: KQuantifier): KExpr<KBoolSort> {
        val bounds = IntPredicatesExtractor(ctx).extractPredicates(expr.bounds.map { it.apply(emptyList()) })
        val bodyPredicates = IntPredicatesExtractor(ctx).extractPredicates(expr.body)

        ints.addAll(bodyPredicates - bounds)
        return expr
    }

    override fun transform(expr: KUniversalQuantifier): KExpr<KBoolSort> = transformQuantifier(expr)

    override fun transform(expr: KExistentialQuantifier): KExpr<KBoolSort> = transformQuantifier(expr)
}
