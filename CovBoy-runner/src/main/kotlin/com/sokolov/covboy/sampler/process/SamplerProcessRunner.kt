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
import com.sokolov.covboy.statistics.getStatisticsFileParam
import com.sokolov.covboy.statistics.hasStatisticsFileParam
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
            val containerName = "cov${Thread.currentThread().id}"
            val processCommand = listOfNotNull(
                "docker", "run", "--rm",
                "--name=$containerName",
                "--memory=${memoryLimit}M",
                "--memory-swap=${memoryLimit}M",
                "-e", "BENCH=${smtLibFormulaFile.name}",
                "-e", "COVERAGE=${outCoverageFile.name}",
                "-v", "${smtLibFormulaFile.parentFile.absolutePath}:/project/benchmarks",
                "-v", "${outCoverageFile.parentFile.absolutePath}:/project/coverage",
                "-e", "SOLVER=$solverType",
                "-e", "SAMPLER=$coverageSamplerType",
                if (coverageSamplerParams.hasSolverTimeoutMillisParam()) "-e" else null,
                if (coverageSamplerParams.hasSolverTimeoutMillisParam()) "SOLVERTIMEOUT=${coverageSamplerParams.getSolverTimeoutMillisParam()}" else null,

                if (coverageSamplerParams.hasCompleteModelsParam()) "-e" else null,
                if (coverageSamplerParams.hasCompleteModelsParam()) "COMPLETEMODELS=${coverageSamplerParams.getCompleteModelsParam()}" else null,

                if (coverageSamplerParams.hasStatisticsFileParam()) "-e" else null,
                if (coverageSamplerParams.hasStatisticsFileParam()) "STATISTICS_FILE=${coverageSamplerParams.getStatisticsFileParam()}" else null,
                "sampler_main"
            )

            val coverageSamplerTimeout = if (coverageSamplerParams.hasSamplerTimeoutMillisParam())
                coverageSamplerParams.getSamplerTimeoutMillisParam().milliseconds
            else DEFAULT_SAMPLER_TIMEOUT

            try {
                // blocks until completed
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

                // blocks until completed
                "docker kill $containerName".split(' ').asProcessRunner().run()

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
