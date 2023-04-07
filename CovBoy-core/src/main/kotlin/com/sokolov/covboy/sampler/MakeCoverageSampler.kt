package com.sokolov.covboy.sampler

import com.sokolov.covboy.sampler.impl.BaselinePredicatePropagatingCoverageSampler
import com.sokolov.covboy.sampler.impl.GroupingModelsCoverageSampler
import com.sokolov.covboy.sampler.impl.UncoveredPredicatesPropagatingCoverageSampler
import com.sokolov.covboy.sampler.params.CoverageSamplerParams
import org.ksmt.KContext
import org.ksmt.expr.KExpr
import org.ksmt.runner.generated.models.SolverType
import org.ksmt.sort.KBoolSort
import org.ksmt.sort.KSort

fun <S : KSort> CoverageSamplerType.makeCoverageSampler(
    solverType: SolverType,
    ctx: KContext,
    assertions: List<KExpr<KBoolSort>>,
    coverageUniverse: Set<KExpr<S>>,
    coveragePredicates: Set<KExpr<S>>,
    params: CoverageSamplerParams
): CoverageSampler<S> = when (this) {
    CoverageSamplerType.BaselinePredicatePropagating -> BaselinePredicatePropagatingCoverageSampler(
        solverType,
        ctx,
        assertions,
        coverageUniverse,
        coveragePredicates,
        params
    )

    CoverageSamplerType.PredicatesPropagatingSampler -> UncoveredPredicatesPropagatingCoverageSampler(
        solverType,
        ctx,
        assertions,
        coverageUniverse,
        coveragePredicates,
        params
    )

    CoverageSamplerType.GroupingModelsSampler -> GroupingModelsCoverageSampler(
        solverType,
        ctx,
        assertions,
        coverageUniverse,
        coveragePredicates,
        params
    )
}
