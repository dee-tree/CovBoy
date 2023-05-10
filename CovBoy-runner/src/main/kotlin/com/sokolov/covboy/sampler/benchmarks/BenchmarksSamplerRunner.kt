package com.sokolov.covboy.sampler.benchmarks

import com.sokolov.covboy.logger
import com.sokolov.covboy.sampler.BenchmarkDataPreprocessor
import com.sokolov.covboy.sampler.CoverageSamplerType
import com.sokolov.covboy.sampler.params.CoverageSamplerParams
import com.sokolov.covboy.sampler.process.SamplerProcessRunner
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.ksmt.runner.generated.models.SolverType
import java.io.File
import java.util.concurrent.Executors

class BenchmarksSamplerRunner {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) = mainBody {
            ArgParser(args).parseInto(::BenchmarksSamplerArgs).run {
                runBenchmarksSampler(
                    benchmarksDir = benchmarksDir,
                    coveragesDir = coverageDir,
                    solvers = solverTypes.toSet(),
                    rewriteResults = rewriteCoverage,
                    coverageSamplerType = coverageSamplerType,
                    coverageSamplerParams = params
                )
            }
        }

        @JvmStatic
        fun runBenchmarksSampler(
            benchmarksDir: File,
            coveragesDir: File,
            solvers: Set<SolverType>,
            coverageSamplerType: CoverageSamplerType,
            coverageSamplerParams: CoverageSamplerParams = CoverageSamplerParams.Empty,
            rewriteResults: Boolean = false
        ) {
            val benchmarks = BenchmarkDataPreprocessor.parseBenchmarks(benchmarksDir).toList()

            // TODO: dispatcher: by solvers count or by processors count?
            val dispatcher = Executors
                .newFixedThreadPool(Runtime.getRuntime().availableProcessors())
                .asCoroutineDispatcher()

            val solversDispatcher = Executors
                .newFixedThreadPool(solvers.size)
                .asCoroutineDispatcher()

            runBlocking {
                withContext(solversDispatcher) {
                    for (solverType: SolverType in solvers) {
                        launch {
                            withContext(dispatcher) {
                                runBenchmarksSampler(
                                    benchmarksDir,
                                    benchmarks,
                                    coveragesDir,
                                    solverType,
                                    coverageSamplerType,
                                    coverageSamplerParams,
                                    rewriteResults
                                )
                            }
                        }
                    }
                }

            }

            solversDispatcher.close()
            dispatcher.close()
        }

        private suspend fun runBenchmarksSampler(
            benchmarksDir: File,
            benchmarks: List<File>,
            coveragesDir: File,
            solver: SolverType,
            coverageSamplerType: CoverageSamplerType,
            coverageSamplerParams: CoverageSamplerParams = CoverageSamplerParams.Empty,
            rewriteResults: Boolean = false
        ) {
            benchmarks.forEachIndexed { benchIdx, benchFile ->
                logger().info("[$solver]: Collect coverage [${benchIdx + 1}] on file [$benchFile]")
                val coverageFile = getCoverageFile(benchFile, solver, benchmarksDir, coveragesDir)

                if (coverageFile.exists() && !rewriteResults) {
                    logger().info("[$solver]: Skip covering on $benchFile due to rewrite = false")
                    return@forEachIndexed // continue
                }

                logger().info("[$solver]: Run coverage process on file [$benchFile]")

                SamplerProcessRunner.runSamplerSmtLibAnotherProcess(
                    solverType = solver,
                    smtLibFormulaFile = benchFile,
                    outCoverageFile = coverageFile,
                    coverageSamplerType = coverageSamplerType,
                    coverageSamplerParams = coverageSamplerParams
                )

            }
        }

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