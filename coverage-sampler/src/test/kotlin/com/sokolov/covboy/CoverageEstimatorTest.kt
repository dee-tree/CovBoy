package com.sokolov.covboy

import com.sokolov.covboy.coverage.CoverageResult
import com.sokolov.covboy.prover.IProver
import com.sokolov.covboy.prover.Prover
import com.sokolov.covboy.prover.SecondaryProver
import com.sokolov.covboy.solvers.supportedTheories
import com.sokolov.covboy.solvers.theories
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.sosy_lab.common.ShutdownManager
import org.sosy_lab.common.configuration.Configuration
import org.sosy_lab.common.log.LogManager
import org.sosy_lab.java_smt.SolverContextFactory
import org.sosy_lab.java_smt.SolverContextFactory.Solvers
import org.sosy_lab.java_smt.api.SolverContext
import java.io.File
import java.util.stream.Stream

abstract class CoverageEstimatorTest {

    @ParameterizedTest
    @MethodSource("provideCoverageEstimatorInput")
    abstract fun test(origin: Solvers, other: Solvers, input: File)

    fun test(coverageSampler: CoverageSamplerProvider, origin: Solvers, other: Solvers, input: File) {
        checkCompatibility(origin, other, input)

        val originProver = makeOriginProver(origin, input)
        val otherProver = makeOtherProver(other, originProver)

        val baseSampler = coverageSampler(originProver)
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
            .listFiles { file: File -> file.isFile && "bug2-15" in file.name }
            ?.map { it } ?: emptyList()

        val excludedSolvers = listOf<Solvers>(
            Solvers.MATHSAT5, // not installed
            Solvers.PRINCESS, // does not support unsat core with assumptions
            Solvers.SMTINTERPOL,
            Solvers.YICES2, // invalid models on boolean_simple
//        SolverContextFactory.Solvers.BOOLECTOR, // crash on intersections boolean_simple (boolector_bv_assignment: cannot retrieve model if input formula is not SAT)
            //Solvers.CVC4
        )

        @JvmStatic
        fun provideCoverageEstimatorInput(): Stream<Arguments> {
            val origin = Solvers.Z3

            val others = Solvers.values().toList() - origin - excludedSolvers

            return Stream.of(
                *buildList {
                    getInputs().map { input ->
                        others.forEach { other ->
                            add(Arguments.of(origin, other, input))
                        }
                    }
                }.toTypedArray()
            )
        }
    }
}

fun checkCompatibility(origin: Solvers, other: Solvers, input: File) {
    val originProver = makeOriginProver(origin, input)

    assumeTrue(other.supportedTheories.containsAll(originProver.theories()))
}

fun makeOriginProver(solver: Solvers, input: File): IProver {
    val shutdownManager = ShutdownManager.create()
    val ctx = SolverContextFactory.createSolverContext(
        Configuration.defaultConfiguration(),
        LogManager.createNullLogManager(),
        shutdownManager.notifier, solver
    )
    val proverEnv = ctx.newProverEnvironment(
        SolverContext.ProverOptions.GENERATE_UNSAT_CORE,
        SolverContext.ProverOptions.GENERATE_MODELS
    )

    return Prover(proverEnv, ctx, input, shutdownManager)
}

fun makeOtherProver(solver: Solvers, origin: IProver): IProver {
    val shutdownManager = ShutdownManager.create()
    val ctx = SolverContextFactory.createSolverContext(
        Configuration.defaultConfiguration(),
        LogManager.createNullLogManager(),
        shutdownManager.notifier, solver
    )

    return SecondaryProver(
        ctx,
        origin.constraints,
        origin,
        shutdownManager
    )
}