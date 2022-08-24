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

    @ParameterizedTest()
    @MethodSource("provideSmtInputPaths")
    fun test(inputPath: String) {
        logger().info("input: $inputPath")

        withContext {
            val solver = solver()
            solver.fromFile(inputPath)

            val coverage = testCoverageSampler(solver, this).getCoverage()
            println("coverage value: ${coverage.coverageNumber}")
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

