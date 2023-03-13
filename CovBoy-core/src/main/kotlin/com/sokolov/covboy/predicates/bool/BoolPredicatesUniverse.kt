package com.sokolov.covboy.predicates.bool

import org.ksmt.KContext
import org.ksmt.expr.KExpr
import org.ksmt.sort.KBoolSort

fun KContext.mkBoolPredicatesUniverse(): Set<KExpr<KBoolSort>> = setOf(mkTrue(), mkFalse())