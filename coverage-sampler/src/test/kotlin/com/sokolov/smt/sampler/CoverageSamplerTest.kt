package com.sokolov.smt.sampler

import com.sokolov.covboy.coverage.CoverageResultWrapper
import com.sokolov.covboy.coverage.CoverageSampler
import com.sokolov.covboy.logger
import com.sokolov.covboy.makeProver
import com.sokolov.covboy.prover.BaseProverEnvironment
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.sosy_lab.java_smt.SolverContextFactory.Solvers
import org.sosy_lab.java_smt.solvers.boolector.BoolectorFormulaCreator
import java.io.File
import java.util.stream.Stream


abstract class CoverageSamplerTest {

    @ParameterizedTest
    @MethodSource("provideSmtInputPaths")
    open fun test(inputPath: String) {

        BoolectorFormulaCreator::class.java.classLoader.setClassAssertionStatus(BoolectorFormulaCreator::class.java.name, false)

        logger().info("input file: $inputPath")

        val prover = provideProver(inputPath)

        val coverage = coverageSampler(prover).computeCoverage()
        println("coverage value for $inputPath: ${coverage.coverageNumber}")
        logger().debug("coverage.solverCheckCalls: ${coverage.solverCheckCalls}")
        logger().debug("Atoms count: ${coverage.atomsCount}")
        logger().debug("Total constrained atoms: ${coverage.atomsCoverage.size}")
        logger().debug("total time coverage: ${coverage.coverageComputationMillis} ms")

        val resFile = getResultFile(inputPath, prover.solverName)

        resFile.writeText(Json.encodeToString(CoverageResultWrapper.fromCoverageResult(prover.solverName, coverage)))
    }

    protected fun getResultFile(inputPath: String, solver: Solvers): File {
        val input = File(inputPath)
        val resultDir = File(input.parentFile, input.nameWithoutExtension)
        resultDir.mkdir()

        return File(resultDir, "${solver.name}.json")
    }

    abstract fun coverageSampler(prover: BaseProverEnvironment): CoverageSampler

    private fun provideProver(inputPath: String): BaseProverEnvironment {
        return makeProver(provideSolver(), File(inputPath))
    }

    abstract fun provideSolver(): Solvers

    companion object {
        @JvmStatic
        fun provideSmtInputPaths(): Stream<Arguments> =
            Stream.of(*File("input")
                .walk()
                .filter { file: File ->
                    file.isFile
                            && file.extension == "smt2"
                            && "(set-info :status unsat)" !in file.readText()
                }
                .toList()
                .filter { "QF_UFBV_bv_bv_adding.1.prop1_ab_cti_max" in it.name }
                .map { it.absolutePath }
                .map { Arguments.of(it) }
                .toTypedArray())
    }

}


