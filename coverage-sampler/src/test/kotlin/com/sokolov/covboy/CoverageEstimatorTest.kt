package com.sokolov.covboy

import com.sokolov.covboy.coverage.CoverageResult
import com.sokolov.covboy.coverage.CoverageResultWrapper
import com.sokolov.covboy.coverage.provider.CoverageSamplerProvider
import com.sokolov.covboy.run.checkCompatibility
import com.sokolov.covboy.solvers.provers.provider.makePrimaryProver
import com.sokolov.covboy.solvers.provers.provider.makeProver
import com.sokolov.covboy.solvers.provers.secondary.SecondaryProver
import com.sokolov.covboy.utils.getPrimaryCoverage
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.sosy_lab.java_smt.SolverContextFactory.Solvers
import java.io.File
import java.util.stream.Stream

abstract class CoverageEstimatorTest {

    @ParameterizedTest
    @MethodSource("provideCoverageEstimatorInput")
    abstract fun test(origin: Solvers, other: Solvers, input: File)

    fun test(coverageSampler: CoverageSamplerProvider, origin: Solvers, other: Solvers, input: File) {
        checkCompatibility(origin, input)
        checkCompatibility(other, input)

        val originProver = makePrimaryProver(origin).apply { addConstraintsFromFile(input) }
        val otherProver = makeProver(false, other).apply { addConstraintsFromFile(input) }

        val baseSampler = coverageSampler(originProver)
        val otherSampler = coverageSampler(otherProver)
        try {
            compare(
                baseSampler.computeCoverage(),
                otherSampler.computeCoverage()
                    .let { if (otherProver is SecondaryProver) otherProver.getPrimaryCoverage(it) else it }
            )
        } catch (e: IllegalStateException) {
            System.err.println("Can't check satisfiability")
            throw e
//            assumeTrue(false, "Can't check satisfiability")
        }
    }


    fun test(coverageSampler: CoverageSamplerProvider, solver: Solvers, input: File): CoverageResultWrapper {
        checkCompatibility(solver, input)

        val prover = makeProver(solver).apply { addConstraintsFromFile(input) }

        val sampler = coverageSampler(prover)

        try {
            return sampler.computeCoverage().let {
                if (prover is SecondaryProver)
                    CoverageResultWrapper.fromCoverageResult(solver, prover.getPrimaryCoverage(it))
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