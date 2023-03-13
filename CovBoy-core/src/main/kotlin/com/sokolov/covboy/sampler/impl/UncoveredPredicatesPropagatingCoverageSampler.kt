package com.sokolov.covboy.sampler.impl

import com.sokolov.covboy.UnknownOnCoverageSamplingException
import com.sokolov.covboy.sampler.CoverageSampler
import org.ksmt.KContext
import org.ksmt.expr.KExpr
import org.ksmt.runner.generated.models.SolverType
import org.ksmt.solver.KSolverStatus
import org.ksmt.sort.KBoolSort
import org.ksmt.sort.KSort


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
class UncoveredPredicatesPropagatingCoverageSampler<S : KSort>(
    solverType: SolverType,
    ctx: KContext,
    assertions: List<KExpr<KBoolSort>>,
    coverageUniverse: Set<KExpr<S>>,
    coveragePredicates: Set<KExpr<S>>,
    completeModels: Boolean = true,
) : CoverageSampler<S>(solverType, ctx, assertions, coverageUniverse, coveragePredicates, completeModels) {

    override fun coverFormula() {

        while (!allPredicatesCovered) {
            solver.push()

            val uncoveredAssignments = uncoveredPredicates.map { it to (coverageUniverse - it.coveredValues).first() }

            val uncoveredAssignmentsDisjunction = ctx.mkOr(uncoveredAssignments.map { (lhs, rhs) -> ctx.mkEq(lhs, rhs) })
            solver.assert(uncoveredAssignmentsDisjunction)

            when (solver.check()) {
                KSolverStatus.SAT -> {
                    coverModel(solver.model())
                }

                KSolverStatus.UNSAT -> {
                    val unsatCore = solver.unsatCore()
                    check(uncoveredAssignmentsDisjunction in unsatCore)

                    uncoveredAssignments.forEach { (lhs, rhs) ->
                        coverPredicateWithUnsatValue(lhs, rhs)
                    }
                }

                KSolverStatus.UNKNOWN -> throw UnknownOnCoverageSamplingException("Unknown on asserting $uncoveredAssignments")
            }

            solver.pop()
        }
    }
}