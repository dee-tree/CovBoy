package com.sokolov.smt.sampler

import com.sokolov.covboy.coverage.CoverageResultWrapper
import com.sokolov.covboy.coverage.sampler.CoverageSampler
import com.sokolov.covboy.logger
import com.sokolov.covboy.solvers.provers.Prover
import com.sokolov.covboy.solvers.provers.provider.makeProver
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

        val prover = makeProver(provideSolver()).apply { addConstraintsFromFile(File(inputPath)) }

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

    abstract fun coverageSampler(prover: Prover): CoverageSampler

    abstract fun provideSolver(): Solvers

    companion object {
        @JvmStatic
        fun provideSmtInputPaths(): Stream<Arguments> =
            Stream.of(*File("input")
                .walk()
                .filter { file: File ->
                    file.isFile
                            && file.extension == "smt2"
                            && file.nameWithoutExtension == "bench_8490"
                            && "(set-info :status unsat)" !in file.readText()
                }
                .toList()
//                .filter { "/home/sokolov/IdeaProjects/CovBoy/coverage-sampler/out/coverage_result/QF_BV/sage/app7/bench_8490" in it.absolutePath }
                .map { it.absolutePath }
                .map { Arguments.of(it) }
                .toTypedArray())
    }

}


