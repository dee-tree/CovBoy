package com.sokolov.covboy.sampler

import com.sokolov.covboy.sampler.impl.BaselinePredicatePropagatingCoverageSampler
import com.sokolov.covboy.sampler.impl.GroupingModelsCoverageSampler
import com.sokolov.covboy.sampler.impl.MultiplePredicatesPropagatingCoverageSampler
import com.sokolov.covboy.sampler.params.CoverageSamplerParams
import com.sokolov.covboy.statistics.MultiplePredicatesPropagatingExtCoverageSampler
import com.sokolov.covboy.statistics.getStatisticsParam
import com.sokolov.covboy.statistics.hasStatisticsParam
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
    CoverageSamplerType.BaselinePredicatePropagatingSampler -> BaselinePredicatePropagatingCoverageSampler(
        solverType,
        ctx,
        assertions,
        coverageUniverse,
        coveragePredicates,
        params
    )

    CoverageSamplerType.PredicatesPropagatingSampler ->
        if (params.hasStatisticsParam() && params.getStatisticsParam())
            MultiplePredicatesPropagatingExtCoverageSampler(
                solverType,
                ctx,
                assertions,
                coverageUniverse,
                coveragePredicates,
                params
            )
        else MultiplePredicatesPropagatingCoverageSampler(
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
