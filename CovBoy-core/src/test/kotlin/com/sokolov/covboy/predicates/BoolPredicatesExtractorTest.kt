package com.sokolov.covboy.predicates

import com.sokolov.covboy.predicates.bool.BoolPredicatesExtractor
import org.ksmt.KContext
import org.ksmt.utils.getValue
import kotlin.test.Test
import kotlin.test.assertEquals

class BoolPredicatesExtractorTest {
    private val ctx = KContext(simplificationMode = KContext.SimplificationMode.NO_SIMPLIFY)


    @Test
    fun boolTheorySimpleTest(): Unit = with(ctx) {
        val a by boolSort
        val b by boolSort
        val c by boolSort

        val f = (a neq b) and (b neq c) and (c neq mkFalse())

        BoolPredicatesExtractor(ctx).extractPredicates(f).also { actualPredicates ->
            assertEquals(setOf(a, b, c), actualPredicates)
        }
    }

    @Test
    fun intTheorySimpleTest(): Unit = with(ctx) {
        val a by intSort
        val b by intSort
        val c by intSort
        val boo by boolSort

        val f = (a gt b) and (boo neq (b eq c)) and (boo eq (b neq c)) and ((c * 100.expr) gt b)

        // b neq c = !(b eq c) ==> we expect b eq c
        // we do not expect <Bool> and <Bool>, !<Bool> and other propositional
        val expected = setOf(
            a gt b,
            b eq c,
            (c * 100.expr) gt b,
            boo
        )

        BoolPredicatesExtractor(ctx).extractPredicates(f).also { actualPredicates ->
            assertEquals(expected, actualPredicates)
        }
    }

    @Test
    fun fpTheorySimpleTest(): Unit = with(ctx) {
        val a by fp16Sort
        val b by fp16Sort
        val boo by boolSort

        val f = mkFpGreaterExpr(mkFpAbsExpr(a), b) and (boo eq mkFpLessExpr(a, b)) and boo

        val expected = setOf(
            mkFpGreaterExpr(mkFpAbsExpr(a), b),
            mkFpLessExpr(a, b),
            boo
        )

        BoolPredicatesExtractor(ctx).extractPredicates(f).also { actualPredicates ->
            assertEquals(expected, actualPredicates)
        }
    }

    @Test
    fun testQuantifier(): Unit = with(ctx) {
        val a by boolSort
        val b by boolSort
        val c by boolSort

        val x by boolSort
        val y by boolSort

        val f1 = (a neq b) and (b neq c) and (c neq mkFalse())
        val f2 = mkExistentialQuantifier((x implies y) or (y implies x), listOf(x.decl, y.decl))

        BoolPredicatesExtractor(ctx).extractPredicates(listOf(f1, f2)).also { actualPredicates ->
            assertEquals(setOf(a, b, c), actualPredicates)
        }
    }

}