package com.sokolov.z3cov

import com.microsoft.z3.Context
import com.microsoft.z3.Solver
import com.microsoft.z3.coverage.CoverageSampler
import com.microsoft.z3.solver
import com.microsoft.z3.withContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.File
import java.util.stream.Stream

abstract class CoverageSamplerTest {

    @ParameterizedTest
    @MethodSource("provideSmtInputPaths")
    open fun test(inputPath: String) {

        logger().info("input file: $inputPath")

        withContext {
            val solver = solver(true)
            solver.fromFile(inputPath)

            val coverage = testCoverageSampler(solver, this).computeCoverage()
            println("coverage value for $inputPath: ${coverage.coverageNumber}")
            logger().debug("coverage.solverCheckCalls: ${coverage.solverCheckCalls}")
            logger().debug("Free atoms portion: ${coverage.freeAtomsPortion}")
            logger().debug("Total constrained atoms: ${coverage.atomsCoverage.size}")
            logger().debug("total time coverage: ${coverage.coverageComputationMillis} ms")

        }

    }

    abstract fun testCoverageSampler(solver: Solver, context: Context): CoverageSampler


    companion object {
        @JvmStatic
        fun provideSmtInputPaths(): Stream<Arguments> =
            Stream.of(*(File("input").listFiles { file: File -> file.isFile }?.map { Arguments.of(it.absolutePath) }
                ?: emptyList()).toTypedArray())
    }

}

