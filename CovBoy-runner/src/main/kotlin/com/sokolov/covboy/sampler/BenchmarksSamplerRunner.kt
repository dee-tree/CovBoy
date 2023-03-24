package com.sokolov.covboy.sampler

import com.sokolov.covboy.logger
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import org.ksmt.runner.generated.models.SolverType
import java.io.File
import java.util.concurrent.Executors
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class BenchmarksSamplerRunner {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val benchmarksDir = File(args[0])
            val coveragesDir = File(args[1])

            val solverTimeoutMillis = args[2].toLong()
            val solverTimeout = solverTimeoutMillis.milliseconds

            val samplerTimeoutMillis = args[3].toLong()
            val samplerTimeout: Duration = samplerTimeoutMillis.milliseconds

            val rewriteResults = args[4].toBooleanStrict()

            val solvers: Set<SolverType> = (5 until args.size).mapTo(hashSetOf()) { solverIdx ->
                SolverType.valueOf(args[solverIdx])
            }

            runBenchmarksSampler(
                benchmarksDir = benchmarksDir,
                coveragesDir = coveragesDir,
                solvers = solvers,
                rewriteResults = rewriteResults,
                solverTimeout = solverTimeout,
                samplerTimeout = samplerTimeout
            )
        }

        @JvmStatic
        fun runBenchmarksSampler(
            benchmarksDir: File,
            coveragesDir: File,
            solvers: Set<SolverType>,
            solverTimeout: Duration = 1.seconds,
            samplerTimeout: Duration = 1.minutes,
            rewriteResults: Boolean = false
        ) {
            val benchmarks = getBenchmarksRecursively(benchmarksDir)

            // TODO: dispatcher: by solvers count or by processors count?
            val dispatcher = Executors
                .newFixedThreadPool(Runtime.getRuntime().availableProcessors())
                .asCoroutineDispatcher()

            benchmarks.forEachIndexed { benchIdx, benchFile ->
                logger().info("Collect coverage [$benchIdx / ${benchmarks.size}] on file [$benchFile]")

                runBlocking {
                    for (solverType: SolverType in solvers) {
                        val coverageFile = getCoverageFile(benchFile, solverType, benchmarksDir, coveragesDir)

                        if (coverageFile.exists() && !rewriteResults) continue

                        SamplerProcessRunner.runSamplerSmtLibAnotherProcess(
                            solverType = solverType,
                            smtLibFormulaFile = benchFile,
                            outCoverageFile = coverageFile,
                            solverTimeout = solverTimeout,
                            coverageSamplerTimeout = samplerTimeout,
                            coroutineContext = this.coroutineContext + dispatcher
                        )
                    }
                }
            }

            dispatcher.close()
        }

        @JvmStatic
        private fun getBenchmarksRecursively(
            benchmarksRootDir: File,
            filter: (File) -> Boolean = { true }
        ): List<File> = benchmarksRootDir
            .walk()
            .filter { file: File ->
                file.isFile
                        && file.extension == "smt2"
                        && "(set-info :status unsat)" !in file.readText()
            }

            .toList()
            .filter(filter)


        @JvmStatic
        fun getCoverageFile(
            benchmark: File,
            solver: SolverType,
            benchmarksRootDir: File,
            coverageRootDir: File
        ): File {
            val benchmarkRelativePath = benchmark.parentFile.relativeTo(benchmarksRootDir).path
            val resultDir = File(File(coverageRootDir, benchmarkRelativePath), benchmark.nameWithoutExtension)
            resultDir.mkdirs()

            return File(resultDir, "$solver.cov")
        }
    }
}