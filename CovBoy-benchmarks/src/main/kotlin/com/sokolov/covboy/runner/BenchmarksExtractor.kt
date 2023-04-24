package com.sokolov.covboy.runner

import com.sokolov.covboy.logger
import com.sokolov.covboy.sampler.BenchmarkDataPreprocessor
import com.sokolov.covboy.sampler.benchmarks.BenchmarksSelector.Companion.satisfiesSatTimeout
import com.sokolov.covboy.trace
import org.ksmt.solver.KSolverStatus
import java.io.File
import kotlin.time.Duration.Companion.seconds

class BenchmarksExtractor {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val benchmarksDir = File(args[0])
            val outFile = File(args[1])

            outFile.bufferedWriter().use { writer ->
                writer.appendLine("benchmark")

                BenchmarkDataPreprocessor.parseBenchmarks(benchmarksDir)
                    .filter { file ->
                        file.satisfiesSatTimeout(KSolverStatus.SAT, 1.seconds).also {
                            if (!it) logger().trace { "Dropped out as unsatisfied on timeout: $file" }
                        }
                    }.forEach {
                        println(it.absolutePath)
                        writer.appendLine(it.absolutePath)
                    }

                writer.flush()
            }

        }

    }
}
