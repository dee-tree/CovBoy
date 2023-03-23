package com.sokolov.covboy.sampler

import com.sokolov.covboy.coverage.PredicatesCoverageSamplingError
import com.sokolov.covboy.coverage.PredicatesCoverageSamplingError.Reasons.ProcessCrashed
import com.sokolov.covboy.coverage.PredicatesCoverageSamplingError.Reasons.TimeoutExceeded
import com.sokolov.covboy.logger
import com.sokolov.covboy.process.asProcessRunner
import kotlinx.coroutines.Dispatchers
import org.ksmt.KContext
import org.ksmt.runner.generated.models.SolverType
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class SamplerProcessRunner {

    companion object {
        suspend fun runSamplerSmtLibAnotherProcess(
            solverType: SolverType,
            smtLibFormulaFile: File,
            outCoverageFile: File,
            solverTimeout: Duration = 1.seconds,
            coverageSamplerTimeout: Duration = 1.minutes,
            coroutineContext: CoroutineContext = Dispatchers.IO
        ) {
            val samplerMainClassName = SamplerMain::class.qualifiedName ?: SamplerMain::class.java.name
            val mainArgs = SamplerMain.makeArgs(solverType, smtLibFormulaFile, outCoverageFile, solverTimeout)
            val classpath = System.getProperty("java.class.path")

            val processCommand = listOf("java", "-classpath", classpath, samplerMainClassName) + mainArgs

            processCommand.asProcessRunner().run(
                timeout = coverageSamplerTimeout,
                onComplete = {

                    if (outCoverageFile.exists()) {
                        logger().info("Coverage process [$it] of [$smtLibFormulaFile] completed")

                    } else {
                        // sampling process crash :)
                        logger().warn("Coverage process [$it] of [$smtLibFormulaFile] crashed!")

                        KContext().use { ctx ->
                            PredicatesCoverageSamplingError(
                                ProcessCrashed,
                                "coverage sampling on solver [$solverType] crashed! Command: ${
                                    processCommand.joinToString(
                                        " "
                                    )
                                }",
                                solverType
                            ).serialize(
                                ctx,
                                outCoverageFile.outputStream()
                            )
                        }
                    }

                },
                onTimeoutExceeded = {
                    logger().warn("Coverage process [$it] of [$smtLibFormulaFile] killed due to sampling timeout")

                    if (outCoverageFile.exists()) {
                        outCoverageFile.delete()
                        outCoverageFile.createNewFile()
                    }

                    KContext().use { ctx ->
                        PredicatesCoverageSamplingError(
                            TimeoutExceeded,
                            "coverage sampling on solver [$solverType] timeout exceeded: $solverTimeout",
                            solverType
                        ).serialize(
                            ctx,
                            outCoverageFile.outputStream()
                        )
                    }
                },
                ctx = coroutineContext
            )
        }
    }
}