package com.sokolov.covboy.sampler.impl

import com.sokolov.covboy.sampler.CoverageSampler
import com.sokolov.covboy.sampler.CoverageSamplerBenchmark
import org.ksmt.KContext
import org.ksmt.expr.KExpr
import org.ksmt.runner.generated.models.SolverType
import org.ksmt.sort.KBoolSort
import kotlin.time.Duration

open class UncoveredPredicatesPropagatingCoverageSamplerBenchmark : CoverageSamplerBenchmark() {
    override fun createCoverageSampler(
        solverType: SolverType,
        ctx: KContext,
        assertions: List<KExpr<KBoolSort>>,
        coverageUniverse: Set<KExpr<KBoolSort>>,
        coveragePredicates: Set<KExpr<KBoolSort>>,
        completeModels: Boolean,
        solverTimeout: Duration
    ): CoverageSampler<KBoolSort> = UncoveredPredicatesPropagatingCoverageSampler(
        solverType,
        ctx,
        assertions,
        coverageUniverse,
        coveragePredicates,
        completeModels,
        solverTimeout
    )
}