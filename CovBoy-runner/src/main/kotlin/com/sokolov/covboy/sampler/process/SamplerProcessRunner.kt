package com.sokolov.covboy.sampler.process

import com.jetbrains.rd.util.TimeoutException
import com.sokolov.covboy.coverage.PredicatesCoverageSamplingError
import com.sokolov.covboy.coverage.PredicatesCoverageSamplingError.Reasons.ProcessCrashed
import com.sokolov.covboy.coverage.PredicatesCoverageSamplingError.Reasons.TimeoutExceeded
import com.sokolov.covboy.logger
import com.sokolov.covboy.process.asProcessRunner
import com.sokolov.covboy.sampler.*
import com.sokolov.covboy.sampler.main.SamplerMain
import com.sokolov.covboy.sampler.params.CoverageSamplerParams
import com.sokolov.covboy.sampler.params.CoverageSamplerParamsBuilder
import org.ksmt.KContext
import org.ksmt.runner.generated.models.SolverType
import java.io.File
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

class SamplerProcessRunner {

    companion object {

        val DEFAULT_SAMPLER_TIMEOUT = 1.minutes

        suspend fun runSamplerSmtLibContainerWithMemLimit(
            solverType: SolverType,
            smtLibFormulaFile: File,
            outCoverageFile: File,
            coverageSamplerType: CoverageSamplerType,
            coverageSamplerParams: CoverageSamplerParams = CoverageSamplerParams.Empty,
            memoryLimit: Int = 4096 // RAM, in MB
        ) {
            val processCommand = listOfNotNull(
                "docker",
                "run",
                "--rm",
                "--memory=${memoryLimit}M",
                "--memory-swap=${memoryLimit}M",
                "-e", "IN_BENCH=$smtLibFormulaFile",
                "-e", "OUT_BENCH=$outCoverageFile",
                "-e", "SOLVER=$solverType",
                "-e", "SAMPLER=$coverageSamplerType",
                if (coverageSamplerParams.hasSolverTimeoutMillisParam()) "-e" else null,
                if (coverageSamplerParams.hasSolverTimeoutMillisParam()) "SOLVERTIMEOUT=${coverageSamplerParams.getSolverTimeoutMillisParam()}" else null,
                if (coverageSamplerParams.hasCompleteModelsParam()) "-e" else null,
                if (coverageSamplerParams.hasCompleteModelsParam()) "COMPLETEMODELS=${coverageSamplerParams.getCompleteModelsParam()}" else null,
                "-v", "/home:/home",
                "-v", "/ssd:/ssd",
                "-v", "/usr:/usr",
                "sampler_main",
                "/bin/bash"
            )

            val coverageSamplerTimeout = if (coverageSamplerParams.hasSamplerTimeoutMillisParam())
                coverageSamplerParams.getSamplerTimeoutMillisParam().milliseconds
            else DEFAULT_SAMPLER_TIMEOUT

            try {
                processCommand.asProcessRunner().run(coverageSamplerTimeout)

                if (outCoverageFile.exists()) {
                    logger().info("[$solverType] Coverage: process completed on $smtLibFormulaFile ")

                } else {
                    // sampling process crash :)
                    logger().warn("[$solverType] Coverage: process crashed on $smtLibFormulaFile")

                    KContext(simplificationMode = KContext.SimplificationMode.NO_SIMPLIFY).use { ctx ->

                        val errorMsg = "coverage sampling on solver [$solverType] crashed! Command: ${
                            processCommand.joinToString(" ")
                        }"

                        PredicatesCoverageSamplingError(ProcessCrashed, errorMsg, solverType).apply {
                            serialize(ctx, outCoverageFile.outputStream())
                        }
                    }
                }
            } catch (e: TimeoutException) {
                logger().warn("[$solverType] Coverage: process killed on timeout $coverageSamplerTimeout on $smtLibFormulaFile")

                if (outCoverageFile.exists()) {
                    outCoverageFile.delete()
                    outCoverageFile.createNewFile()
                }

                KContext(simplificationMode = KContext.SimplificationMode.NO_SIMPLIFY).use { ctx ->
                    PredicatesCoverageSamplingError(
                        TimeoutExceeded,
                        "coverage sampling on solver [$solverType] timeout exceeded: $coverageSamplerTimeout",
                        solverType
                    ).serialize(ctx, outCoverageFile.outputStream())
                }
            }
        }

        suspend fun runSamplerSmtLibAnotherProcess(
            solverType: SolverType,
            smtLibFormulaFile: File,
            outCoverageFile: File,
            coverageSamplerType: CoverageSamplerType,
            coverageSamplerParams: CoverageSamplerParams = CoverageSamplerParams.Empty,
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

            try {
                processCommand.asProcessRunner().run(coverageSamplerTimeout)

                if (outCoverageFile.exists()) {
                    logger().info("[$solverType] Coverage: process completed on $smtLibFormulaFile ")

                } else {
                    // sampling process crash :)
                    logger().warn("[$solverType] Coverage: process crashed on $smtLibFormulaFile")

                    KContext(simplificationMode = KContext.SimplificationMode.NO_SIMPLIFY).use { ctx ->

                        val errorMsg = "coverage sampling on solver [$solverType] crashed! Command: ${
                            processCommand.joinToString(" ")
                        }"

                        PredicatesCoverageSamplingError(ProcessCrashed, errorMsg, solverType).apply {
                            serialize(ctx, outCoverageFile.outputStream())
                        }
                    }
                }
            } catch (e: TimeoutException) {
                logger().warn("[$solverType] Coverage: process killed on timeout $coverageSamplerTimeout on $smtLibFormulaFile")

                if (outCoverageFile.exists()) {
                    outCoverageFile.delete()
                    outCoverageFile.createNewFile()
                }

                KContext(simplificationMode = KContext.SimplificationMode.NO_SIMPLIFY).use { ctx ->
                    PredicatesCoverageSamplingError(
                        TimeoutExceeded,
                        "coverage sampling on solver [$solverType] timeout exceeded: $coverageSamplerTimeout",
                        solverType
                    ).serialize(ctx, outCoverageFile.outputStream())
                }
            }

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
