package com.sokolov.covboy

import com.sokolov.covboy.predicates.bool.mkBoolPredicatesUniverse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.ksmt.KContext
import org.ksmt.sort.KBoolSort
import org.ksmt.utils.getValue
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.assertEquals

class PredicatesCoverageSerializerTest {
    private val ctx = KContext()

    @Test
    fun testInOutSerialization(): Unit = with(ctx) {
        val a by boolSort
        val b by boolSort
        val f = a and ((a xor b) implies (b))

        val coverage = PredicatesCoverage(
            mapOf(a to setOf(true.expr), b to setOf(true.expr), f to setOf(true.expr, false.expr)),
            mapOf(a to setOf(false.expr), b to setOf(false.expr)),
            mkBoolPredicatesUniverse()
        )

        val out = ByteArrayOutputStream()

        with(PredicatesCoverageSerializer(ctx)) {
            coverage.serialize(out)
        }

        val input = ByteArrayInputStream(out.toByteArray())

        val deserializedCoverage = PredicatesCoverageSerializer(ctx).deserialize<KBoolSort>(input)

        assertEquals(coverage, deserializedCoverage)
    }


    @Test
    fun testDataCorrupted(): Unit = with(ctx) {
        val a by boolSort
        val b by boolSort
        val f = a and ((a xor b) implies (b))

        val coverage = PredicatesCoverage(
            mapOf(a to setOf(true.expr), b to setOf(true.expr), f to setOf(true.expr, false.expr)),
            mapOf(a to setOf(false.expr), b to setOf(false.expr)),
            mkBoolPredicatesUniverse()
        )

        val out = ByteArrayOutputStream()
        out.write(-7)
        out.write(13)
        out.write(5)

        with(PredicatesCoverageSerializer(ctx)) {
            coverage.serialize(out)
        }

        val input = ByteArrayInputStream(out.toByteArray())

        assertThrows<IllegalStateException> {
            PredicatesCoverageSerializer(ctx).deserialize<KBoolSort>(input)
        }
    }
}