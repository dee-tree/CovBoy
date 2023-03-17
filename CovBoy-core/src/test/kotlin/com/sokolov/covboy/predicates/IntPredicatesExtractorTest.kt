package com.sokolov.covboy.predicates

import com.sokolov.covboy.predicates.integer.IntPredicatesExtractor
import org.ksmt.KContext
import org.ksmt.utils.getValue
import kotlin.test.Test
import kotlin.test.assertEquals

class IntPredicatesExtractorTest {
    private val ctx = KContext()


    @Test
    fun simpleTest(): Unit = with(ctx) {
        val a by intSort
        val b by intSort
        val c by intSort

        val f = (a ge b) and (b gt c) and (c gt 0.expr) and (b gt 5.expr)

        IntPredicatesExtractor(ctx).extractPredicates(f).also { actualPredicates ->
            assertEquals(setOf(a, b, c), actualPredicates)
        }
    }

    @Test
    fun testQuantifier(): Unit = with(ctx) {
        val a by intSort
        val b by intSort
        val c by intSort

        val x by intSort
        val y by intSort

        val f1 = (a ge b) and (b gt c) and (c gt 0.expr) and (b gt 5.expr)
        val qf = ((x gt y) and (y gt (-1).expr)) implies (x ge 0.expr)
        val f2 = mkExistentialQuantifier(qf, listOf(x.decl, y.decl))

        IntPredicatesExtractor(ctx).extractPredicates(listOf(f1, f2)).also { actualPredicates ->
            assertEquals(setOf(a, b, c), actualPredicates)
        }
    }

}