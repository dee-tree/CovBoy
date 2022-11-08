package com.sokolov.covboy

import com.sokolov.covboy.coverage.CoverageResult
import com.sokolov.covboy.coverage.CoverageResultWrapper
import com.sokolov.covboy.prover.BaseProverEnvironment
import com.sokolov.covboy.prover.Prover
import com.sokolov.covboy.prover.secondary.SecondaryProver
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
import org.sosy_lab.java_smt.solvers.boolector.createExtendedBoolectorSolverContext
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
        try {
            compare(
                baseSampler.computeCoverage(),
                otherSampler.computeCoverage()
                    .let { if (otherProver is SecondaryProver) otherProver.getOriginalCoverage(it) else it }
            )
        } catch (e: IllegalStateException) {
            System.err.println("Can't check satisfiability")
            throw e
//            assumeTrue(false, "Can't check satisfiability")
        }
    }


    fun test(coverageSampler: CoverageSamplerProvider, solver: Solvers, input: File): CoverageResultWrapper {
        checkCompatibility(solver, input)

        val prover = makeProver(solver, input)

        val sampler = coverageSampler(prover)

        try {
            return sampler.computeCoverage().let {
                if (prover is SecondaryProver)
                    CoverageResultWrapper.fromCoverageResult(solver, prover.getOriginalCoverage(it))
                else CoverageResultWrapper.fromCoverageResult(solver, it)
            }

        } catch (e: IllegalStateException) {
            System.err.println("Can't check satisfiability")
            throw e
//            assumeTrue(false, "Can't check satisfiability")
        }
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
            .walk()
            .filter { file: File ->
                file.isFile
                        && file.extension == "smt2"
                        && "(set-info :status unsat)" !in file.readText()
//                    && "QF_LIA" in file.absolutePath
//                    file.parent == "input"
            }
//            .take(1)
            .toList()
            .filter { it.parent == "input" }

        val excludedSolvers = listOf<Solvers>(
            Solvers.MATHSAT5, // not installed
//            Solvers.PRINCESS,
//            Solvers.SMTINTERPOL,
            Solvers.YICES2, // invalid models on boolean_simple (required isUnsat check before model get, because it returns empty models)
//            Solvers.BOOLECTOR,
//            Solvers.CVC4
        )

        @JvmStatic
        fun provideInputFileArgs(): Stream<Arguments> = Stream.of(
            *getInputs().map { Arguments.of(it) }.toTypedArray()
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

fun checkCompatibility(solver: Solvers, input: File) {
    val prover = makeOriginProver(Solvers.Z3, input)

    if (prover.theories().any { it !in solver.supportedTheories }) {
        System.err.println("Prover $solver does not support ${prover.theories() - solver.supportedTheories} needed theories")
    }
    assumeTrue(solver.supportedTheories.containsAll(prover.theories()))
}

fun checkCompatibility(origin: Solvers, other: Solvers, input: File) {
    val originProver = makeOriginProver(origin, input)

    if (originProver.theories().any { it !in other.supportedTheories }) {
        System.err.println("Prover $other does not support ${originProver.theories() - other.supportedTheories} needed theories")
    }
    assumeTrue(other.supportedTheories.containsAll(originProver.theories()))
}

fun makeOriginProver(solver: Solvers, input: File): BaseProverEnvironment {
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

    return Prover(proverEnv, ctx, input)
}

fun makeProver(solver: Solvers, input: File): BaseProverEnvironment {
    return if (solver == Solvers.Z3) {
        makeOriginProver(solver, input)
    } else {
        makeOtherProver(solver, makeOriginProver(Solvers.Z3, input))
    }
}

fun makeOtherProver(solver: Solvers, origin: BaseProverEnvironment): BaseProverEnvironment {
    val ctx = if (solver == Solvers.BOOLECTOR) createExtendedBoolectorSolverContext()
    else SolverContextFactory.createSolverContext(solver)

    return SecondaryProver(
        ctx,
        origin
    )
}