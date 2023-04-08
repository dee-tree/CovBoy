package com.sokolov.covboy.sampler

import com.sokolov.covboy.parseAssertions
import com.sokolov.covboy.predicates.bool.BoolPredicatesExtractor
import com.sokolov.covboy.predicates.bool.mkBoolPredicatesUniverse
import org.ksmt.KContext
import org.ksmt.expr.KExpr
import org.ksmt.runner.generated.models.SolverType
import org.ksmt.sort.KBoolSort
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

@State(Scope.Thread)
abstract class CoverageSamplerBenchmark {

    lateinit var coverageSampler: CoverageSampler<KBoolSort>

    lateinit var ctx: KContext

    @Param("Z3")
    lateinit var solverType: SolverType

    //    @Param("true", "false")
    @Param("true")
    var completeModels: Boolean = true

    @Param(
        "a_and_b",
        "a_or_b_or_c_or_d_or_etc",
        "QF_BV_problem_1",
    )
    lateinit var smt2BenchmarkName: String

    private val smt2BenchmarkContent: String
        get() = this::class.java.classLoader
            .getResourceAsStream("smt2-formulas/$smt2BenchmarkName.smt2")
            .bufferedReader()
            .readText()

    abstract fun createCoverageSampler(
        solverType: SolverType,
        ctx: KContext,
        assertions: List<KExpr<KBoolSort>>,
        coverageUniverse: Set<KExpr<KBoolSort>>,
        coveragePredicates: Set<KExpr<KBoolSort>>,
        completeModels: Boolean = true,
        solverTimeout: Duration = Duration.INFINITE
    ): CoverageSampler<KBoolSort>

    @Setup(Level.Iteration)
    fun initContext() {
        ctx = KContext(simplificationMode = KContext.SimplificationMode.NO_SIMPLIFY)
    }

    @TearDown(Level.Iteration)
    fun closeContext() {
        ctx.close()
    }

    @Setup(Level.Invocation)
    fun initCoverageSampler() {
        val assertions = ctx.parseAssertions(smt2BenchmarkContent)
        val predicates = BoolPredicatesExtractor(ctx).extractPredicates(assertions)

        coverageSampler = createCoverageSampler(
            solverType,
            ctx,
            assertions,
            ctx.mkBoolPredicatesUniverse(),
            predicates,
            completeModels
        )
    }

    @TearDown(Level.Invocation)
    fun closeCoverageSampler() {
        coverageSampler.close()
    }

    @Benchmark
    @Fork(5)
    @Warmup(iterations = 5)
    @BenchmarkMode(Mode.AverageTime)
    @Measurement(iterations = 5, time = 60, timeUnit = TimeUnit.SECONDS)
    fun work(bh: Blackhole) {
        bh.consume(coverageSampler.computeCoverage())
    }
}