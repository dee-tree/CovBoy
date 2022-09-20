package com.sokolov.z3cov

import com.microsoft.z3.*
import com.microsoft.z3.coverage.CoverageSampler
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
            val atomsCount = solver.atoms.size

            val coverage = coverageSampler(solver, this).computeCoverage()
            println("coverage value for $inputPath: ${coverage.coverageNumber}")
            logger().debug("coverage.solverCheckCalls: ${coverage.solverCheckCalls}")
            logger().debug("Atoms count: $atomsCount (free: ${coverage.freeAtoms})")
            logger().debug("Free atoms portion: ${coverage.freeAtomsPortion}")
            logger().debug("Total constrained atoms: ${coverage.atomsCoverage.size}")
            logger().debug("total time coverage: ${coverage.coverageComputationMillis} ms")

        }

    }

    abstract fun coverageSampler(solver: Solver, context: Context): CoverageSampler


    companion object {
        @JvmStatic
        fun provideSmtInputPaths(): Stream<Arguments> =
            Stream.of(*(File("input").listFiles { file: File -> file.isFile }?.map { Arguments.of(it.absolutePath) }
                ?: emptyList()).toTypedArray())
    }

}

