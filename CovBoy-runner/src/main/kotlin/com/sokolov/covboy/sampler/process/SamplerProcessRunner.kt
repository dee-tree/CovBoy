package com.sokolov.covboy.sampler.process

import com.sokolov.covboy.coverage.PredicatesCoverageSamplingError
import com.sokolov.covboy.coverage.PredicatesCoverageSamplingError.Reasons.ProcessCrashed
import com.sokolov.covboy.coverage.PredicatesCoverageSamplingError.Reasons.TimeoutExceeded
import com.sokolov.covboy.logger
import com.sokolov.covboy.process.asProcessRunner
import com.sokolov.covboy.sampler.CoverageSamplerType
import com.sokolov.covboy.sampler.main.SamplerMain
import com.sokolov.covboy.sampler.params.CoverageSamplerParams
import com.sokolov.covboy.sampler.params.CoverageSamplerParamsBuilder
import kotlinx.coroutines.Dispatchers
import org.ksmt.KContext
import org.ksmt.runner.generated.models.SolverType
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

class SamplerProcessRunner {

    companion object {

        val DEFAULT_SAMPLER_TIMEOUT = 1.minutes

        suspend fun runSamplerSmtLibAnotherProcess(
            solverType: SolverType,
            smtLibFormulaFile: File,
            outCoverageFile: File,
            coverageSamplerType: CoverageSamplerType,
            coverageSamplerParams: CoverageSamplerParams = CoverageSamplerParams.Empty,
            coroutineContext: CoroutineContext = Dispatchers.IO
        ) {
            val samplerMainClassName = SamplerMain::class.qualifiedName ?: SamplerMain::class.java.name
            val mainArgs = SamplerMain.makeArgs(
                solverType,
                smtLibFormulaFile,
                outCoverageFile,
                coverageSamplerType,
                coverageSamplerParams
            )
            val classpath = System.getProperty("java.class.path")

            val processCommand = listOf("java", "-classpath", classpath, samplerMainClassName) + mainArgs

            val coverageSamplerTimeout = if (coverageSamplerParams.hasSamplerTimeoutMillisParam())
                coverageSamplerParams.getSamplerTimeoutMillisParam().milliseconds
            else DEFAULT_SAMPLER_TIMEOUT

            processCommand.asProcessRunner().run(
                timeout = coverageSamplerTimeout,
                onComplete = {

                    if (outCoverageFile.exists()) {
                        logger().info("$solverType Coverage: process completed [$it] | [$smtLibFormulaFile] ")

                    } else {
                        // sampling process crash :)
                        logger().warn("$solverType Coverage: process crashed! [$it] | [$smtLibFormulaFile]")

                        KContext(simplificationMode = KContext.SimplificationMode.NO_SIMPLIFY).use { ctx ->
                            PredicatesCoverageSamplingError(
                                ProcessCrashed,
                                "coverage sampling on solver [$solverType] crashed! Command: ${
                                    processCommand.joinToString(" ")
                                }",
                                solverType
                            ).serialize(ctx, outCoverageFile.outputStream())
                        }
                    }

                },
                onTimeoutExceeded = {
                    logger().warn("$solverType Coverage: process killed on timeout $coverageSamplerTimeout. [$it] | [$smtLibFormulaFile]")

                    if (outCoverageFile.exists()) {
                        outCoverageFile.delete()
                        outCoverageFile.createNewFile()
                    }

                    KContext(simplificationMode = KContext.SimplificationMode.NO_SIMPLIFY).use { ctx ->
                        PredicatesCoverageSamplingError(
                            TimeoutExceeded,
                            "coverage sampling on solver [$solverType] timeout exceeded: $coverageSamplerTimeout",
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

    object ParamKeys {
        const val SamplerTimeoutMillis = "SamplerTimeoutMillis"
    }
}

fun CoverageSamplerParams.hasSamplerTimeoutMillisParam(): Boolean =
    hasLongParam(SamplerProcessRunner.ParamKeys.SamplerTimeoutMillis)

fun CoverageSamplerParams.getSamplerTimeoutMillisParam(): Long =
    getLong(SamplerProcessRunner.ParamKeys.SamplerTimeoutMillis)

fun CoverageSamplerParamsBuilder.putSamplerTimeoutMillis(value: Long) {
    putParam(SamplerProcessRunner.ParamKeys.SamplerTimeoutMillis, value)
}
