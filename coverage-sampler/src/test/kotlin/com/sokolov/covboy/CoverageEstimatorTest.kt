package com.sokolov.covboy

import com.sokolov.covboy.coverage.CoverageResult
import com.sokolov.covboy.prover.IProver
import com.sokolov.covboy.prover.Prover
import com.sokolov.covboy.prover.SecondaryProver
import org.junit.jupiter.params.provider.Arguments
import org.sosy_lab.java_smt.SolverContextFactory
import org.sosy_lab.java_smt.api.SolverContext
import java.io.File
import java.util.stream.Stream
import kotlin.test.assertEquals

abstract class CoverageEstimatorTest {
    fun test(coverageSampler: CoverageSamplerProvider, baseProver: IProver, otherProver: IProver) {
        val baseSampler = coverageSampler(baseProver)
        val otherSampler = coverageSampler(otherProver)
        compare(
            baseSampler.computeCoverage(),
            otherSampler.computeCoverage()
                .let { if (otherProver is SecondaryProver) otherProver.getOriginalCoverage(it) else it }
        )
    }

    private fun compare(baseResult: CoverageResult, anotherResult: CoverageResult) {

        assert(baseResult.compareTo(anotherResult) == 0) { diffAsString(baseResult, anotherResult) }
    }

    private fun diffAsString(baseResult: CoverageResult, anotherResult: CoverageResult): String =
        "$baseResult\n$anotherResult\n" +
    if (baseResult < anotherResult) {
            "Base coverage < another coverage!\n" +
                    "Difference: ${baseResult.diff(anotherResult)}"
        } else {
            "Base coverage > another coverage!\n" +
                    "Difference: ${baseResult.diff(anotherResult)}"
        }


    companion object {

        fun getInputs(): List<File> = File("input")
            .listFiles { file: File -> file.isFile && "simple" in file.name }
            ?.map { it } ?: emptyList()

        val baseSolver = listOf(
            SolverContextFactory.Solvers.Z3
        )
        val excludedSolvers = listOf(
            SolverContextFactory.Solvers.MATHSAT5, // not installed
//            SolverContextFactory.Solvers.PRINCESS, // does not support unsat core with assumptions
        SolverContextFactory.Solvers.YICES2, // invalid models on boolean_simple
        SolverContextFactory.Solvers.BOOLECTOR, // crash on intersections boolean_simple (boolector_bv_assignment: cannot retrieve model if input formula is not SAT)
        SolverContextFactory.Solvers.CVC4
        )

        private fun getBaseProver(baseContext: SolverContext, input: File): IProver = baseContext.newProverEnvironment(
            SolverContext.ProverOptions.GENERATE_MODELS,
            SolverContext.ProverOptions.ENABLE_SEPARATION_LOGIC,
            SolverContext.ProverOptions.GENERATE_UNSAT_CORE,
            SolverContext.ProverOptions.GENERATE_UNSAT_CORE_OVER_ASSUMPTIONS
        ).let { Prover(it, baseContext, input) }

        private fun getOtherProver(context: SolverContext, baseProver: IProver): IProver = SecondaryProver(
            context,
            baseProver.constraints,
            baseProver
        )

        @JvmStatic
        fun provideCoverageEstimatorInput(): Stream<Arguments> {
            val baseContext = SolverContextFactory.createSolverContext(SolverContextFactory.Solvers.Z3)

            val otherContexts =
                (SolverContextFactory.Solvers.values().toList() - baseSolver - excludedSolvers).map {
//                listOf(SolverContextFactory.Solvers.SMTINTERPOL).map {
                    SolverContextFactory.createSolverContext(it)
                }

            return Stream.of(
                *buildList {
                    getInputs().map { input ->
                        otherContexts.forEach {
                            val base = getBaseProver(baseContext, input)
                            val other = getOtherProver(it, base)
                            add(Arguments.of(base, other))
                        }
                    }
                }.toTypedArray()
            )
        }
    }
}