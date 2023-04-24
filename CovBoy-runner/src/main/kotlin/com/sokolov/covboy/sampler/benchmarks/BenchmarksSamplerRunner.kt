package com.sokolov.covboy.sampler.benchmarks

import com.sokolov.covboy.logger
import com.sokolov.covboy.sampler.BenchmarkDataPreprocessor
import com.sokolov.covboy.sampler.CoverageSamplerType
import com.sokolov.covboy.sampler.params.CoverageSamplerParams
import com.sokolov.covboy.sampler.process.SamplerProcessRunner
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
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
            val benchmarks = BenchmarkDataPreprocessor.parseBenchmarks(benchmarksDir)

            // TODO: dispatcher: by solvers count or by processors count?
            val dispatcher = Executors
                .newFixedThreadPool(Runtime.getRuntime().availableProcessors())
                .asCoroutineDispatcher()

            benchmarks.forEachIndexed { benchIdx, benchFile ->
                logger().info("Collect coverage [${benchIdx + 1}] on file [$benchFile]")

                runBlocking {
                    for (solverType: SolverType in solvers) {
                        val coverageFile = getCoverageFile(benchFile, solverType, benchmarksDir, coveragesDir)

                        if (coverageFile.exists() && !rewriteResults) {
                            logger().info("Skip $solverType coverage on $benchFile")
                            continue
                        }

                        logger().info("Run sampler process $solverType on benchmark [$benchFile]")

                        SamplerProcessRunner.runSamplerSmtLibAnotherProcess(
                            solverType = solverType,
                            smtLibFormulaFile = benchFile,
                            outCoverageFile = coverageFile,
                            coroutineContext = this.coroutineContext + dispatcher,
                            coverageSamplerType = coverageSamplerType,
                            coverageSamplerParams = coverageSamplerParams
                        )
                    }
                }
            }

            dispatcher.close()
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