package com.sokolov.covboy.bench

import com.sokolov.covboy.data.CheckSatData
import com.sokolov.covboy.data.SamplerBenchmarkData
import com.sokolov.covboy.parseAssertions
import com.sokolov.covboy.predicates.bool.BoolPredicatesExtractor
import com.sokolov.covboy.predicates.bool.mkBoolPredicatesUniverse
import com.sokolov.covboy.sampler.CoverageSampler
import com.sokolov.covboy.sampler.CoverageSamplerExt
import com.sokolov.covboy.sampler.CoverageSamplerType
import com.sokolov.covboy.sampler.makeCoverageSamplerExt
import com.sokolov.covboy.sampler.params.CoverageSamplerParams
import org.ksmt.KContext
import org.ksmt.runner.generated.models.SolverType
import org.ksmt.solver.KSolverStatus
import org.ksmt.sort.KBoolSort
import org.ksmt.utils.uncheckedCast
import java.io.File
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

open class CoverageSamplerBenchmark(
    val inputFormula: File,
    val repeatCount: Int = 1,
    val solverType: SolverType,
    val samplerType: CoverageSamplerType,
    val samplerParams: CoverageSamplerParams = CoverageSamplerParams.Empty,
    private val ctx: KContext = KContext(simplificationMode = KContext.SimplificationMode.NO_SIMPLIFY)
) {

    lateinit var sampler: CoverageSampler<KBoolSort>

    private val samplerExt: CoverageSamplerExt<KBoolSort>
        get() = sampler.uncheckedCast()

    private lateinit var currentBenchCheckSats: MutableList<CheckSatData>
    private var currentBenchPreviousCheckSatCoveredValues = 0

    private lateinit var benchIterations: MutableList<SamplerBenchmarkData>

//    private lateinit var previousCheckSatData: CheckSatData

    private fun createSampler(): CoverageSampler<KBoolSort> {
        val assertions = ctx.parseAssertions(inputFormula)

        val predicates = BoolPredicatesExtractor(ctx).extractPredicates(assertions)

        return samplerType.makeCoverageSamplerExt(
            solverType,
            ctx,
            assertions,
            ctx.mkBoolPredicatesUniverse(),
            predicates,
            samplerParams,
            ::onCheckSatMeasured
        )
    }

    @ExperimentalTime
    fun run(): List<SamplerBenchmarkData> {
        repeat(repeatCount) {
            beforeMeasure()

            measureTime { measure() }.also { benchIterDuration ->
                benchIterations += SamplerBenchmarkData(
                    inputFormula.absolutePath,
                    benchIterDuration,
                    currentBenchCheckSats.size,
                    currentBenchCheckSats,
                    sampler.coveragePredicates.size,
                    samplerType,
                    solverType
                )
            }

            afterMeasure()
        }

        return benchIterations
    }

    protected open fun beforeMeasure() {
        sampler = createSampler()

        currentBenchCheckSats = mutableListOf()
        currentBenchPreviousCheckSatCoveredValues = 0
        benchIterations = mutableListOf()
    }

    protected open fun measure() {
        sampler.computeCoverage()
    }

    protected open fun afterMeasure() {
        sampler.close()
    }


    private fun onCheckSatMeasured(status: KSolverStatus, duration: Duration) {
        val coveredValuesCount = samplerExt.coveredSatValuesCount
        currentBenchCheckSats += CheckSatData(
            duration,
            status,
            coveredValuesCount - currentBenchPreviousCheckSatCoveredValues
        )
        currentBenchPreviousCheckSatCoveredValues = coveredValuesCount
    }
}