package com.sokolov.covboy.predicates.integer

import org.ksmt.KContext
import org.ksmt.expr.KExpr
import org.ksmt.sort.KIntSort

fun KContext.mkIntPredicatesUniverse(): Set<KExpr<KIntSort>> =
    setOf((-1).expr, 0.expr, 1.expr) + (-1000..1000 step 10).mapTo(hashSetOf<KExpr<KIntSort>>()) { it.expr }