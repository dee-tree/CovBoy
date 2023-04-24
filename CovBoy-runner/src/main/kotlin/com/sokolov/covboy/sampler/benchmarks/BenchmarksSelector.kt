package com.sokolov.covboy.sampler.benchmarks

import com.sokolov.covboy.sampler.BenchmarkDataPreprocessor
import com.sokolov.covboy.sampler.preprocessCoverageSamplerAssertions
import org.ksmt.KContext
import org.ksmt.solver.KSolverStatus
import org.ksmt.solver.z3.KZ3Solver
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class BenchmarksSelector {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val benchmarksDir = File(args[0])
            val maxBenchmarksCount = args[1].toInt()

            println("Benchmarks: ")

            BenchmarkDataPreprocessor.parseBenchmarks(benchmarksDir)
                .shuffled()
                .onEach { println(it.absolutePath) }
                .take(maxBenchmarksCount)
        }

        fun File.satisfiesSatTimeout(satStatus: KSolverStatus, timeout: Duration = 1.seconds): Boolean {
            var satisfies = false
            try {
                satisfies = KContext(simplificationMode = KContext.SimplificationMode.NO_SIMPLIFY).use { ctx ->
                    KZ3Solver(ctx).use { solver ->
                        ctx.preprocessCoverageSamplerAssertions(this).forEach(solver::assert)
                        solver.check(timeout)
                    } == satStatus
                }
            } catch (e: NotImplementedError) {
                System.err.println(e)
            } catch (e: Exception) {
                System.err.println(e)
            }
            return satisfies
        }

    }
}