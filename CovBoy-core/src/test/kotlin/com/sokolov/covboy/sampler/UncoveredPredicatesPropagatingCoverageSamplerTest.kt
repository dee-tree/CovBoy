package com.sokolov.covboy.sampler

import com.sokolov.covboy.predicates.bool.BoolPredicatesExtractor
import com.sokolov.covboy.predicates.bool.mkBoolPredicatesUniverse
import com.sokolov.covboy.predicates.integer.IntPredicatesExtractor
import com.sokolov.covboy.predicates.integer.transformIntPredicates
import com.sokolov.covboy.sampler.impl.UncoveredPredicatesPropagatingCoverageSampler
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.ksmt.KContext
import org.ksmt.runner.generated.models.SolverType
import org.ksmt.utils.getValue
import kotlin.test.assertContains
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UncoveredPredicatesPropagatingCoverageSamplerTest {
    val ctx = KContext(simplificationMode = KContext.SimplificationMode.NO_SIMPLIFY)

    @ParameterizedTest
    @EnumSource(value = SolverType::class, names = ["Bitwuzla"], mode = EnumSource.Mode.EXCLUDE)
    fun testSimple(solverType: SolverType): Unit = with(ctx) {
        val a by boolSort
        val b by boolSort
        val i1 by intSort
        val i2 by intSort

        val f1 = a eq (i1 ge 0.expr)
        val f2 = b eq (i2 gt i1)

        val boolPredicatesExtractor = BoolPredicatesExtractor(ctx)
        val predicates = boolPredicatesExtractor.extractPredicates(listOf(f1, f2))

        val sampler = UncoveredPredicatesPropagatingCoverageSampler(
            solverType,
            ctx,
            listOf(f1, f2),
            ctx.mkBoolPredicatesUniverse(),
            predicates,
        )

        sampler.computeCoverage().also { coverage ->
            predicates.forEach { predicate ->
                assertTrue { coverage.isCovered(predicate) }
                assertContains(coverage.coverageSat.getValue(predicate), mkTrue())
                assertContains(coverage.coverageSat.getValue(predicate), mkFalse())

                assertTrue { coverage.coverageUnsat.getValue(predicate).isEmpty() }
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = SolverType::class, names = ["Bitwuzla"], mode = EnumSource.Mode.EXCLUDE)
    fun testSimpleInt(solverType: SolverType): Unit = with(ctx) {
        val i by intSort

        val f = i gt 0.expr

        val intPredicatesExtractor = IntPredicatesExtractor(ctx)
        val intPredicates = intPredicatesExtractor.extractPredicates(listOf(f))
        val transformedPredicates = transformIntPredicates(intPredicates)

        println("int predicates: $intPredicates")
        println("transformed predicates: $transformedPredicates")

        val sampler = UncoveredPredicatesPropagatingCoverageSampler(
            solverType,
            ctx,
            listOf(f),
            ctx.mkBoolPredicatesUniverse(),
            transformedPredicates,
        )

        sampler.computeCoverage().also { coverage ->
            transformedPredicates.forEach { predicate ->
                assertTrue { coverage.isCovered(predicate) }

                when (predicate) {
                    i gt 0.expr -> {
                        assertContains(coverage.coverageSat.getValue(predicate), mkTrue())
                        assertFalse { mkTrue() in coverage.coverageUnsat.getValue(predicate) }

                        assertContains(coverage.coverageUnsat.getValue(predicate), mkFalse())
                        assertFalse { mkFalse() in coverage.coverageSat.getValue(predicate) }
                    }
                    i lt 0.expr -> {
                        assertContains(coverage.coverageSat.getValue(predicate), mkFalse())
                        assertFalse { mkFalse() in coverage.coverageUnsat.getValue(predicate) }

                        assertContains(coverage.coverageUnsat.getValue(predicate), mkTrue())
                        assertFalse { mkTrue() in coverage.coverageSat.getValue(predicate) }
                    }
                    i eq 0.expr -> {
                        assertContains(coverage.coverageSat.getValue(predicate), mkFalse())
                        assertFalse { mkFalse() in coverage.coverageUnsat.getValue(predicate) }

                        assertContains(coverage.coverageUnsat.getValue(predicate), mkTrue())
                        assertFalse { mkTrue() in coverage.coverageSat.getValue(predicate) }
                    }
                }
            }
        }

    }

}