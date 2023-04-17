package com.sokolov.covboy.sampler

import com.sokolov.covboy.sampler.impl.BaselinePredicatePropagatingExtCoverageSampler
import com.sokolov.covboy.sampler.impl.MultiplePredicatesPropagatingExtCoverageSampler
import com.sokolov.covboy.sampler.params.CoverageSamplerParams
import org.ksmt.KContext
import org.ksmt.expr.KExpr
import org.ksmt.runner.generated.models.SolverType
import org.ksmt.solver.KSolverStatus
import org.ksmt.sort.KBoolSort
import org.ksmt.sort.KSort
import kotlin.time.Duration

fun <S : KSort> CoverageSamplerType.makeCoverageSamplerExt(
    solverType: SolverType,
    ctx: KContext,
    assertions: List<KExpr<KBoolSort>>,
    coverageUniverse: Set<KExpr<S>>,
    coveragePredicates: Set<KExpr<S>>,
    params: CoverageSamplerParams = CoverageSamplerParams.Empty,
    onCheckSatMeasured: (KSolverStatus, Duration) -> Unit = { _, _ -> },
): CoverageSampler<S> = when (this) {
    CoverageSamplerType.BaselinePredicatePropagatingSampler -> BaselinePredicatePropagatingExtCoverageSampler(
        solverType,
        ctx,
        assertions,
        coverageUniverse,
        coveragePredicates,
        params,
        onCheckSatMeasured
    )

    CoverageSamplerType.PredicatesPropagatingSampler -> MultiplePredicatesPropagatingExtCoverageSampler(
        solverType,
        ctx,
        assertions,
        coverageUniverse,
        coveragePredicates,
        params,
        onCheckSatMeasured
    )

    CoverageSamplerType.GroupingModelsSampler -> TODO()
}
