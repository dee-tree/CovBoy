package com.sokolov.covboy.sampler.impl

import com.sokolov.covboy.sampler.CoverageSampler
import com.sokolov.covboy.sampler.params.CoverageSamplerParams
import org.ksmt.KContext
import org.ksmt.expr.KExpr
import org.ksmt.runner.generated.models.SolverType
import org.ksmt.solver.KSolverStatus
import org.ksmt.sort.KBoolSort
import org.ksmt.sort.KSort
import kotlin.time.Duration

open class BaselinePredicatePropagatingCoverageSampler<S : KSort> : CoverageSampler<S> {
    constructor(
        solverType: SolverType,
        ctx: KContext,
        assertions: List<KExpr<KBoolSort>>,
        coverageUniverse: Set<KExpr<S>>,
        coveragePredicates: Set<KExpr<S>>,
        completeModels: Boolean = DEFAULT_COMPLETE_MODELS,
        solverTimeout: Duration = DEFAULT_SOLVER_TIMEOUT
    ) : super(
        solverType,
        ctx,
        assertions,
        coverageUniverse,
        coveragePredicates,
        completeModels,
        solverTimeout
    )

    constructor(
        solverType: SolverType,
        ctx: KContext,
        assertions: List<KExpr<KBoolSort>>,
        coverageUniverse: Set<KExpr<S>>,
        coveragePredicates: Set<KExpr<S>>,
        params: CoverageSamplerParams
    ) : super(
        solverType,
        ctx,
        assertions,
        coverageUniverse,
        coveragePredicates,
        params
    )

    override fun coverFormula() {
        while (!allPredicatesCovered) {
            solver.push()

            val predicate = uncoveredPredicates.first { predicate ->
                (coverageUniverse - predicate.coveredValues).any {
                    !isUnknownPredicateValue(predicate, it)
                }
            }

            val value = (coverageUniverse - predicate.coveredValues).first { !isUnknownPredicateValue(predicate, it) }

            solver.assert(ctx.mkEq(predicate, value))

            when (solver.checkWithTimeout()) {
                KSolverStatus.SAT -> {
                    coverModel(solver.model())
                }

                KSolverStatus.UNSAT -> {
                    if (value !in predicate.coveredValues)
                        coverPredicateWithUnsatValue(predicate, value)
                }

                KSolverStatus.UNKNOWN -> {
                    markAsUnknown(predicate, value)
                }
            }

            solver.pop()
        }
    }
}
