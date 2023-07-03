package com.sokolov.covboy.sampler.benchmarks

import com.sokolov.covboy.logger
import com.sokolov.covboy.sampler.CoverageSamplerType
import com.sokolov.covboy.sampler.params.CoverageSamplerParams
import com.sokolov.covboy.sampler.process.SamplerProcessRunner
import com.sokolov.covboy.statistics.*
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
            val benchmarks = File("/ssd/sokolov/IdeaProjects/CovBoy/CovBoy-runner/data/benchmarks/benchs-1s.csv")
                .useLines { lines ->
                    lines.drop(1).map { File(it) }.toList()
                } //BenchmarkDataPreprocessor.parseBenchmarks(benchmarksDir).toList()

            // TODO: dispatcher: by solvers count or by processors count?
            val dispatcher = Executors
//                .newFixedThreadPool(Runtime.getRuntime().availableProcessors())
                .newFixedThreadPool(solvers.size)
                .asCoroutineDispatcher()

            runBlocking {
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

                var newParams = coverageSamplerParams.copy()

                if (coverageSamplerParams.hasStatisticsParam() && coverageSamplerParams.getStatisticsParam()) {
                    val statisticsDir = File(coverageSamplerParams.getStatisticsFileParam())
                    val statisticsFile = getStatisticsFile(benchFile, solver, benchmarksDir, statisticsDir)

                    newParams += CoverageSamplerParams.build {
                        putStatistics(true)
                        putStatisticsFile(statisticsFile.absolutePath)
                    }
                }

                logger().info("[$solver]: Run coverage process on file [$benchFile]")

                /*SamplerProcessRunner.runSamplerSmtLibAnotherProcess(
                    solverType = solver,
                    smtLibFormulaFile = benchFile,
                    outCoverageFile = coverageFile,
                    coverageSamplerType = coverageSamplerType,
                    coverageSamplerParams = newParams,
                )*/

                SamplerProcessRunner.runSamplerSmtLibContainerWithMemLimit(
                    solverType = solver,
                    smtLibFormulaFile = benchFile,
                    outCoverageFile = coverageFile,
                    coverageSamplerType = coverageSamplerType,
                    coverageSamplerParams = newParams,
                    memoryLimit = 4096
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

        @JvmStatic
        fun getStatisticsFile(
            benchmark: File,
            solver: SolverType,
            benchmarksRootDir: File,
            statisticsRootDir: File
        ): File {
            val benchmarkRelativePath = benchmark.parentFile.relativeTo(benchmarksRootDir).path
            val resultDir = File(File(statisticsRootDir, benchmarkRelativePath), benchmark.nameWithoutExtension)
            resultDir.mkdirs()

            return File(resultDir, "$solver.csv")
        }
    }
}