package com.sokolov.covboy.coverage

import com.sokolov.covboy.coverage.sampler.impl.makeBoolGroupingModelsCoverageSampler
import com.sokolov.covboy.utils.KBoolExpr
import org.ksmt.KContext
import org.ksmt.solver.KSolver
import org.ksmt.solver.bitwuzla.KBitwuzlaSolver
import org.ksmt.solver.z3.KZ3Solver


abstract class CoverageSamplerTestBase {

    companion object {
        @JvmStatic
        fun provideBoolSamplerTests(): Iterable<DynamicSamplerTestInput> =
            buildList {
                boolSamplerExamples.forEach { makeData ->
                    boolSamplers.forEach { makeSampler ->
                        solvers.forEach { makeSolver ->
                            val ctx = KContext()
                            val solver = makeSolver(ctx)

                            val testCase = makeData(ctx)
                            testCase.assertions.forEach(solver::assert)

                            val sampler = makeSampler(ctx, solver, testCase.assertions)

                            add(DynamicSamplerTestInput(testCase, sampler))
                        }
                    }
                }
            }
    }
}

val boolSamplers = listOf(
    { ctx: KContext, solver: KSolver<*>, assertions: List<KBoolExpr> ->
        makeBoolGroupingModelsCoverageSampler(
            ctx,
            solver,
            assertions,
            2
        )
    },
    { ctx: KContext, solver: KSolver<*>, assertions: List<KBoolExpr> ->
        makeBoolGroupingModelsCoverageSampler(
            ctx,
            solver,
            assertions,
            3
        )
    },
    { ctx: KContext, solver: KSolver<*>, assertions: List<KBoolExpr> ->
        makeBoolGroupingModelsCoverageSampler(
            ctx,
            solver,
            assertions,
            5
        )
    },
)

val solvers = listOf(
    { ctx: KContext -> KZ3Solver(ctx) },
    { ctx: KContext -> KBitwuzlaSolver(ctx) }
)

