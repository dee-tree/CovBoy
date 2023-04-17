package com.sokolov.covboy.sampler.impl

import com.sokolov.covboy.KSolverExt
import com.sokolov.covboy.sampler.CoverageSamplerExt
import com.sokolov.covboy.sampler.params.CoverageSamplerParams
import org.ksmt.KContext
import org.ksmt.expr.KExpr
import org.ksmt.runner.generated.models.SolverType
import org.ksmt.solver.KSolver
import org.ksmt.solver.KSolverStatus
import org.ksmt.sort.KBoolSort
import org.ksmt.sort.KSort
import kotlin.time.Duration

class MultiplePredicatesPropagatingExtCoverageSampler<S : KSort> :
    MultiplePredicatesPropagatingCoverageSampler<S>,
    CoverageSamplerExt<S> {

    private lateinit var onCheckSatMeasured: (KSolverStatus, Duration) -> Unit

    constructor(
        solverType: SolverType,
        ctx: KContext,
        assertions: List<KExpr<KBoolSort>>,
        coverageUniverse: Set<KExpr<S>>,
        coveragePredicates: Set<KExpr<S>>,
        completeModels: Boolean = DEFAULT_COMPLETE_MODELS,
        solverTimeout: Duration = DEFAULT_SOLVER_TIMEOUT,
        onCheckSatMeasured: (KSolverStatus, Duration) -> Unit = { _, _ -> },
    ) : super(solverType, ctx, assertions, coverageUniverse, coveragePredicates, completeModels, solverTimeout) {
        this.onCheckSatMeasured = onCheckSatMeasured
    }

    constructor(
        solverType: SolverType,
        ctx: KContext,
        assertions: List<KExpr<KBoolSort>>,
        coverageUniverse: Set<KExpr<S>>,
        coveragePredicates: Set<KExpr<S>>,
        params: CoverageSamplerParams,
        onCheckSatMeasured: (KSolverStatus, Duration) -> Unit = { _, _ -> },
    ) : super(solverType, ctx, assertions, coverageUniverse, coveragePredicates, params) {
        this.onCheckSatMeasured = onCheckSatMeasured
    }


    override val solver: KSolver<*> = KSolverExt(
        super.solver
    ) { status, duration -> onCheckSatMeasured(status, duration) }

    override val coveredSatValuesCount: Int
        get() = coveragePredicates.sumOf { it.coveredSatValues.size }

}
