package com.sokolov.covboy.sampler

import com.sokolov.covboy.predicates.bool.BoolPredicatesExtractor
import com.sokolov.covboy.predicates.bool.mkBoolPredicatesUniverse
import com.sokolov.covboy.predicates.integer.IntPredicatesExtractor
import com.sokolov.covboy.predicates.integer.mkIntPredicatesUniverse
import com.sokolov.covboy.sampler.impl.UncoveredPredicatesPropagatingCoverageSampler
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.ksmt.KContext
import org.ksmt.runner.generated.models.SolverType
import org.ksmt.utils.getValue

class UncoveredPredicatesPropagatingCoverageSamplerTest {
    val ctx = KContext()

    @ParameterizedTest
    @EnumSource(value = SolverType::class, names = ["Bitwuzla"], mode = EnumSource.Mode.EXCLUDE)
    fun simpleTest(solverType: SolverType): Unit = with(ctx) {
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
            predicates.forEach(coverage::isCovered)
            predicates.forEach { it in coverage.coverageSat.getValue(it) }
        }

    }

    @ParameterizedTest
    @EnumSource(value = SolverType::class, names = ["Bitwuzla"], mode = EnumSource.Mode.EXCLUDE)
    fun simpleTestInts(solverType: SolverType): Unit = with(ctx) {
        val a by boolSort
        val b by boolSort
        val i1 by intSort
        val i2 by intSort

        val f1 = a eq (i1 ge 0.expr)
        val f2 = b eq (i2 gt i1)

        val intPredicatesExtractor = IntPredicatesExtractor(ctx)
        val predicates = intPredicatesExtractor.extractPredicates(listOf(f1, f2))

        val sampler = UncoveredPredicatesPropagatingCoverageSampler(
            solverType,
            ctx,
            listOf(f1, f2),
            ctx.mkIntPredicatesUniverse(),
            predicates,
        )

        sampler.computeCoverage().also { coverage ->
            predicates.forEach(coverage::isCovered)
            predicates.forEach { it in coverage.coverageSat.getValue(it) }
        }

    }
}