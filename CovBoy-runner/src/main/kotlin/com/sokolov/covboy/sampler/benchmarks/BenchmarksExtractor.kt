package com.sokolov.covboy.sampler.benchmarks

import com.sokolov.covboy.logger
import com.sokolov.covboy.sampler.BenchmarkDataPreprocessor
import com.sokolov.covboy.sampler.preprocessCoverageSamplerAssertions
import com.sokolov.covboy.trace
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import org.ksmt.KContext
import org.ksmt.solver.KSolverStatus
import org.ksmt.solver.z3.KZ3Solver
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

class BenchmarksExtractor {

    companion object {
        @JvmStatic
        @ExperimentalTime
        fun main(args: Array<String>) = mainBody("BenchmarksExtractor") {
            ArgParser(args).parseInto(::Args).run {
                (outFile?.bufferedWriter() ?: System.out.bufferedWriter()).use { writer ->
                    writer.appendLine("benchmark")

                    BenchmarkDataPreprocessor.parseBenchmarks(benchmarksDir)
                        .let { seq -> if (shuffled) seq.shuffled() else seq }
                        .let { seq ->
                            if (checkSatTimeout == Duration.ZERO) seq
                            else seq.filter { file ->
                                file.satisfiesSatTimeout(KSolverStatus.SAT, 1.seconds).also {
                                    if (!it) logger().trace { "Dropped out as unsatisfied on timeout: $file" }
                                }
                            }
                        }
                        .let { seq -> if (benchmarksCountLimit > 0) seq.take(benchmarksCountLimit) else seq }
                        .forEach {
                            logger().trace { it.absolutePath }
                            writer.appendLine(it.absolutePath)
                        }

                    writer.flush()
                }
            }

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

    class Args(parser: ArgParser) {

        val benchmarksDir: File by parser.positional(
            "BENCHMARKS",
            "Directory with smtlib benchmarks"
        ) {
            File(this)
        }

        val benchmarksCountLimit: Int by parser.storing(
            "--maxBenchmarks", "--max",
            help = "Limit of selected benchmarks. 0 - without limit"
        ) { this.toInt() }
            .default(0)
            .addValidator { if (value < 0) throw IllegalArgumentException("Max benchmarks count must be non-negative") }

        val shuffled: Boolean by parser.flagging("--shuffle", "-s", help = "Shuffle benchmarks")
            .default(false)

        val checkSatTimeoutMillis: Long by parser.storing(
            "--timeout", "-t", "--checksattimeout",
            help = "Timeout on check-sat for each benchmark (in millis). 0 - without timeout"
        ) { this.toLong() }
            .default(0)
            .addValidator { if (value < 0) throw IllegalArgumentException("timeout must be non-negative") }

        val checkSatTimeout: Duration
            get() = checkSatTimeoutMillis.milliseconds

        val threadsCount: Int by parser.storing(
            "--threads", "-p",
            help = "Max count of threads for parallel benchmarks extraction. By default - all available processors"
        ) { this.toInt() }
            .default(Runtime.getRuntime().availableProcessors())
            .addValidator { if (value <= 0) throw IllegalArgumentException("Threads count must be positive") }

        val outFile: File? by parser.storing(
            "--out", "-o",
            help = "File to write extracted benchmarks per line with header - csv format"
        ) { File(this) }
            .default(null)
    }
}