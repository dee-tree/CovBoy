package com.sokolov.covboy.predicates

import com.sokolov.covboy.predicates.bool.BoolPredicatesExtractor
import org.ksmt.KContext
import org.ksmt.utils.getValue
import kotlin.test.Test
import kotlin.test.assertEquals

class BoolPredicatesExtractorTest {
    private val ctx = KContext()


    @Test
    fun simpleTest(): Unit = with(ctx) {
        val a by boolSort
        val b by boolSort
        val c by boolSort

        val f = (a neq b) and (b neq c) and (c neq mkFalse())

        BoolPredicatesExtractor(ctx).extractPredicates(f).also { actualPredicates ->
            assertEquals(setOf(a, b, c), actualPredicates)
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