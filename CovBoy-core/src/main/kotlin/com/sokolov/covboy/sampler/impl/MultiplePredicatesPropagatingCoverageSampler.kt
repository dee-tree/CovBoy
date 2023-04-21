package com.sokolov.covboy.sampler.impl

import com.sokolov.covboy.sampler.CoverageSampler
import com.sokolov.covboy.sampler.exceptions.UnknownSolverStatusOnCoverageSamplingException
import com.sokolov.covboy.sampler.params.CoverageSamplerParams
import org.ksmt.KContext
import org.ksmt.expr.KExpr
import org.ksmt.runner.generated.models.SolverType
import org.ksmt.solver.KSolverStatus
import org.ksmt.sort.KBoolSort
import org.ksmt.sort.KSort
import kotlin.time.Duration

/**
 * a: ?, b: ?, c: ? |
 * ------------------
 *
 * push()
 * assert(a == true || b == true || c == true)  // assert uncovered assignments
 * a) sat
 *  * coverModel() // a: true, b: true, ...
 * b) unsat
 *  * coverUnsatCore(): // a: true, b: true, c: true
 * pop()
 *
 * repeat for uncovered values
 */
open class MultiplePredicatesPropagatingCoverageSampler<S : KSort> : CoverageSampler<S> {
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

            val uncoveredAssignments = uncoveredPredicates.map { it to (coverageUniverse - it.coveredValues).first() }

            val uncoveredAssignmentsDisjunction = ctx.mkOr(
                uncoveredAssignments.map { (lhs, rhs) -> ctx.mkEq(lhs, rhs) }
            )

            solver.assert(uncoveredAssignmentsDisjunction)

            when (solver.checkWithTimeout()) {
                KSolverStatus.SAT -> {
                    coverModel(solver.model())
                }

                KSolverStatus.UNSAT -> {
                    uncoveredAssignments.forEach { (lhs, rhs) ->
                        coverPredicateWithUnsatValue(lhs, rhs)
                    }
                }

                KSolverStatus.UNKNOWN -> throw UnknownSolverStatusOnCoverageSamplingException("Unknown on asserting $uncoveredAssignments")
            }

            solver.pop()
        }
    }
}
