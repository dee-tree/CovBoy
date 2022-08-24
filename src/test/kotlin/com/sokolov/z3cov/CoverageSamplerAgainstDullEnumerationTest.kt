package com.sokolov.z3cov

import com.microsoft.z3.Context
import com.microsoft.z3.Solver
import com.microsoft.z3.coverage.CoverageResult
import com.microsoft.z3.coverage.ModelsEnumerationCoverage
import com.microsoft.z3.solver
import com.microsoft.z3.withContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertContains
import kotlin.test.assertTrue

abstract class CoverageSamplerAgainstDullEnumerationTest : CoverageSamplerTest() {

    @ParameterizedTest
    @MethodSource("provideSmtInputPaths")
    fun compareWithDullEnumeration(inputPath: String) {
        logger().info("input: $inputPath")

        val coverage: CoverageResult
        val dullEnumCoverage: CoverageResult

        withContext {
            val solver1 = solver()
            solver1.fromFile(inputPath)
            coverage = testCoverageSampler(solver1, this).getCoverage()

            val solver2 = solver()
            solver2.fromFile(inputPath)
            dullEnumCoverage = dullEnumCoverageSampler(solver2, this).getCoverage()
        }

        assertTrue { coverage.coverageNumber <= dullEnumCoverage.coverageNumber }

        dullEnumCoverage.atomsCoverage.forEach { (boolExpr, idealCov) ->
            assertContains(coverage.atomsCoverage.keys, boolExpr)
            assertTrue { coverage.atomsCoverage[boolExpr]!! <= idealCov }
        }

        logger().debug("coverage number: ${coverage.coverageNumber}")
        logger().debug("ideal coverage number: ${dullEnumCoverage.coverageNumber}")
        /**
         * < 0 => worse than dull enumeration
         * = 0 => same as dull enumeration
         */
        logger().info("coverage difference: ${coverage.coverageNumber - dullEnumCoverage.coverageNumber}")

        logger().debug("coverage.solverCheckCalls vs ideal coverage.solverCheckCalls: ${coverage.solverCheckCalls} VS ${dullEnumCoverage.solverCheckCalls}")

        assertTrue(message = "coverage.solverCheckCalls is greater than dullEnumCoverage.solverCheckCalls: ${coverage.solverCheckCalls} > ${dullEnumCoverage.solverCheckCalls}") {
            coverage.solverCheckCalls <= dullEnumCoverage.solverCheckCalls
        }

        val totalCoverageSpentTimeMillis = with(coverage) {
            coverageComputationMillis + coveringModelsComputationMillis
        }
        val totalDullCoverageSpentTimeMillis = with(dullEnumCoverage) {
            coverageComputationMillis + coveringModelsComputationMillis
        }
        logger().debug("total time coverage vs dull enum time coverage: $totalCoverageSpentTimeMillis ms VS $totalDullCoverageSpentTimeMillis ms")

        assertTrue(message = "total coverage spent time is greater than dullEnumCoverage: $totalCoverageSpentTimeMillis > $totalDullCoverageSpentTimeMillis") {
            coverage.solverCheckCalls <= dullEnumCoverage.solverCheckCalls
        }
    }
}

private fun dullEnumCoverageSampler(solver: Solver, context: Context): ModelsEnumerationCoverage =
    ModelsEnumerationCoverage(solver, context)


