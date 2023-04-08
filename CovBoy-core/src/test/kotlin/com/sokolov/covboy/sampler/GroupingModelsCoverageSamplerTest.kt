package com.sokolov.covboy.sampler

import com.sokolov.covboy.logger
import com.sokolov.covboy.parseAssertions
import com.sokolov.covboy.predicates.bool.BoolPredicatesExtractor
import com.sokolov.covboy.predicates.bool.mkBoolPredicatesUniverse
import com.sokolov.covboy.predicates.integer.IntPredicatesExtractor
import com.sokolov.covboy.predicates.integer.transformIntPredicates
import com.sokolov.covboy.sampler.impl.GroupingModelsCoverageSampler
import com.sokolov.covboy.sampler.impl.UncoveredPredicatesPropagatingCoverageSampler
import kotlinx.coroutines.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.MethodSource
import org.ksmt.KContext
import org.ksmt.runner.generated.models.SolverType
import org.ksmt.utils.getValue
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GroupingModelsCoverageSamplerTest {
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

        val sampler = GroupingModelsCoverageSampler(
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

        val sampler = GroupingModelsCoverageSampler(
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

                    else -> error("unexpected predicate")
                }
            }
        }

    }

    @ParameterizedTest
    @MethodSource("smtBenchmarkToSolverArgs")
    fun testCoverageOnPropagatingSampler(smtBenchmarkName: String, solverType: SolverType) = with(ctx) {
        val formula = this::class.java.classLoader
            .getResourceAsStream("smt2-formulas/$smtBenchmarkName.smt2")!!
            .bufferedReader().readText()

        val assertions = parseAssertions(formula)

        val boolPredicatesExtractor = BoolPredicatesExtractor(ctx)
        val predicates = boolPredicatesExtractor.extractPredicates(assertions)

        val sampler = GroupingModelsCoverageSampler(
            solverType,
            ctx,
            assertions,
            ctx.mkBoolPredicatesUniverse(),
            predicates
        )

        val propagatingSampler = UncoveredPredicatesPropagatingCoverageSampler(
            solverType,
            ctx,
            assertions,
            ctx.mkBoolPredicatesUniverse(),
            predicates
        )

        logger().trace("Before propagating coverage sampling")
        val propagatingCoverage = propagatingSampler.computeCoverage()
        logger().trace("Propagating coverage collected!")

        val groupingCoverage = sampler.computeCoverage()
        logger().trace("Models grouping coverage collected!")

        assertEquals(propagatingCoverage, groupingCoverage)

    }

    companion object {

        private val smtBenchmarkNames = listOf(
//            "a_and_b",
//            "a_or_b_or_c_or_d_or_etc",
            "QF_BV_bv8_bv_cyclic_scheduler.2.prop1_cc_ref_max",
//            "bitops7",
//            "Sz128_2824"
        )

        private val smtBenchmarkSolvers = listOf(
            SolverType.Z3,
//            SolverType.Bitwuzla,
//            SolverType.Cvc5,
//            SolverType.Yices
        )

        @JvmStatic
        private fun smtBenchmarkToSolverArgs(): List<Arguments> = buildList {
            smtBenchmarkNames.forEach { benchmarkName ->
                smtBenchmarkSolvers.forEach { solverName ->
                    this += Arguments.of(benchmarkName, solverName)
                }
            }
        }
    }

}