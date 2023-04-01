package com.sokolov.covboy.sampler

import com.sokolov.covboy.coverage.PredicatesCoverage
import com.sokolov.covboy.sampler.params.CoverageSamplerParams
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.ksmt.KContext
import org.ksmt.runner.generated.models.SolverType
import org.ksmt.sort.KBoolSort
import org.ksmt.utils.getValue
import java.io.File
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class SamplerRunnerTest {

    @field:TempDir
    lateinit var tempDir: File

    @ParameterizedTest
    @EnumSource(value = SolverType::class)
    fun testSamplerRun(solverType: SolverType) {
        val input = File.createTempFile("smtlib2-formula", null, tempDir)
        input.writeText(
            """
            (declare-const a Bool)
            (declare-const b Bool)
            
            (assert (and a b))
        """.trimIndent()
        )

        val outputCoverageFile = File.createTempFile("coverage-test", null, tempDir)

        SamplerMain.runSamplerSmtLib(
            solverType,
            input,
            outputCoverageFile,
            CoverageSamplerType.PredicatesPropagatingSampler //TODO: allow here to run another coverage sampler type
        )

        val ctx = KContext()
        val coverage = assertDoesNotThrow {
            PredicatesCoverage.deserialize<KBoolSort>(ctx, outputCoverageFile.inputStream())
        }

        assertEquals(solverType, coverage.solverType)

        with(ctx) {
            val a by boolSort
            val b by boolSort

            assertTrue { coverage.isCovered(a) }
            assertTrue { coverage.isCovered(b) }

            assertContains(coverage.coverageSat.getValue(a), mkTrue())
            assertContains(coverage.coverageSat.getValue(b), mkTrue())
            assertContains(coverage.coverageUnsat.getValue(a), mkFalse())
            assertContains(coverage.coverageUnsat.getValue(b), mkFalse())
        }
    }


    @ParameterizedTest
    @EnumSource(value = SolverType::class)
    fun testSamplerRunProcess(solverType: SolverType) {
        val input = File.createTempFile("smtlib2-formula", null, tempDir)
        input.writeText(
            """
            (declare-const a Bool)
            (declare-const b Bool)
            
            (assert (and a b))
        """.trimIndent()
        )

        val outputCoverageFile = File.createTempFile("coverage-test", null, tempDir)

        runBlocking {
            SamplerProcessRunner.runSamplerSmtLibAnotherProcess(
                solverType,
                input,
                outputCoverageFile,
                coroutineContext = this.coroutineContext,
                coverageSamplerType = CoverageSamplerType.PredicatesPropagatingSampler, // TODO: test all CoverageSamplerType
                coverageSamplerParams = CoverageSamplerParams.build { putSamplerTimeoutMillis(5.seconds.inWholeMilliseconds) }
            )
        }
        // after process finished

        val ctx = KContext()
        val coverage = assertDoesNotThrow {
            PredicatesCoverage.deserialize<KBoolSort>(ctx, outputCoverageFile.inputStream())
        }

        assertEquals(solverType, coverage.solverType)

        with(ctx) {
            val a by boolSort
            val b by boolSort

            assertTrue { coverage.isCovered(a) }
            assertTrue { coverage.isCovered(b) }

            assertContains(coverage.coverageSat.getValue(a), mkTrue())
            assertContains(coverage.coverageSat.getValue(b), mkTrue())
            assertContains(coverage.coverageUnsat.getValue(a), mkFalse())
            assertContains(coverage.coverageUnsat.getValue(b), mkFalse())
        }
    }

}