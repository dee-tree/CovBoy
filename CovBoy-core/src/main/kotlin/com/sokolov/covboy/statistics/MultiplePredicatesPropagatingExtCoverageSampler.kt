package com.sokolov.covboy.statistics

import com.sokolov.covboy.sampler.CoverageSamplerType
import com.sokolov.covboy.sampler.impl.MultiplePredicatesPropagatingCoverageSampler
import com.sokolov.covboy.sampler.params.CoverageSamplerParams
import org.ksmt.KContext
import org.ksmt.expr.KExpr
import org.ksmt.runner.generated.models.SolverType
import org.ksmt.solver.KSolver
import org.ksmt.solver.KSolverStatus
import org.ksmt.sort.KBoolSort
import org.ksmt.sort.KSort
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime


class MultiplePredicatesPropagatingExtCoverageSampler<S : KSort> :
    MultiplePredicatesPropagatingCoverageSampler<S>,
    CoverageSamplerExt<S> {

    private val currentBenchCheckSats = mutableListOf<CheckSatStatistics>()
    private var previousCheckSatCoveredValues = 0

    /*
     * store previous check-sat to associate covered predicates for the previous call
     * check-sat1 -> cover1() -> check-sat2
     * We need to have covered predicates count for cover1 and put it to check-sat1 data
     */
    private lateinit var previousCheckSatData: CheckSatStatistics

    private var coverageDuration: Duration = 0.milliseconds

    override val statistics: SamplerStatistics
        get() = SamplerStatistics(
            coverageDuration,
            currentBenchCheckSats.size,
            currentBenchCheckSats,
            coveragePredicates.size,
            CoverageSamplerType.PredicatesPropagatingSampler
        )

    private fun onCheckSatMeasured(status: KSolverStatus, duration: Duration) {
        val coveredValuesCount = coveredSatValuesCount
        val coveredValuesByThisStep = coveredValuesCount - previousCheckSatCoveredValues

        if (::previousCheckSatData.isInitialized) {
            currentBenchCheckSats += previousCheckSatData.copy(coveredPredicates = coveredValuesByThisStep)
        }

        previousCheckSatData = CheckSatStatistics(
            duration,
            status,
            coveredValuesByThisStep
        )
        previousCheckSatCoveredValues = coveredValuesCount
    }

    constructor(
        solverType: SolverType,
        ctx: KContext,
        assertions: List<KExpr<KBoolSort>>,
        coverageUniverse: Set<KExpr<S>>,
        coveragePredicates: Set<KExpr<S>>,
        completeModels: Boolean = DEFAULT_COMPLETE_MODELS,
        solverTimeout: Duration = DEFAULT_SOLVER_TIMEOUT,
    ) : super(solverType, ctx, assertions, coverageUniverse, coveragePredicates, completeModels, solverTimeout)

    constructor(
        solverType: SolverType,
        ctx: KContext,
        assertions: List<KExpr<KBoolSort>>,
        coverageUniverse: Set<KExpr<S>>,
        coveragePredicates: Set<KExpr<S>>,
        params: CoverageSamplerParams,
    ) : super(solverType, ctx, assertions, coverageUniverse, coveragePredicates, params)


    override val solver: KSolver<*> = KSolverExt(super.solver) { status, duration ->
        onCheckSatMeasured(status, duration)
    }

    @OptIn(ExperimentalTime::class)
    override fun coverFormula() {
        measureTime { super.coverFormula() }.also { coverageDuration = it }

        // add last check-sat data
        val coveredValuesByThisStep = coveredSatValuesCount - previousCheckSatCoveredValues
        currentBenchCheckSats += previousCheckSatData.copy(coveredPredicates = coveredValuesByThisStep)
    }

    override val coveredSatValuesCount: Int
        get() = coveragePredicates.sumOf { it.coveredSatValues.size }

}
