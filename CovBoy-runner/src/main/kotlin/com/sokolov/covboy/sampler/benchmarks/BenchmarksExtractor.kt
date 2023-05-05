package com.sokolov.covboy.sampler.benchmarks

import com.sokolov.covboy.logger
import com.sokolov.covboy.sampler.BenchmarkDataPreprocessor
import com.sokolov.covboy.sampler.preprocessCoverageSamplerAssertions
import com.sokolov.covboy.trace
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import com.xenomachina.argparser.mainBody
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.ksmt.KContext
import org.ksmt.expr.KExpr
import org.ksmt.solver.KSolver
import org.ksmt.solver.KSolverStatus
import org.ksmt.solver.async.KAsyncSolver
import org.ksmt.solver.runner.KSolverRunnerManager
import org.ksmt.solver.z3.KZ3SMTLibParser
import org.ksmt.solver.z3.KZ3Solver
import org.ksmt.sort.KBoolSort
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.Executors
import kotlin.time.Duration
import kotlin.time.Duration.Companion.INFINITE
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

class BenchmarksExtractor {

    companion object {
        @JvmStatic
        @ExperimentalTime
        fun main(args: Array<String>) = mainBody("BenchmarksExtractor") {
            ArgParser(args).parseInto(::Args).run {

                KContext(simplificationMode = KContext.SimplificationMode.NO_SIMPLIFY).use { ctx ->
                    val solverManager = KSolverRunnerManager(
                        workerPoolSize = threadsCount,
                        hardTimeout = checkSatTimeout * 10
                    )

                    val parser = KZ3SMTLibParser(ctx)
                    val dispatcher = Executors.newFixedThreadPool(threadsCount).asCoroutineDispatcher()

                    runBlocking {
                        val scope = this

                        (outFile?.let { FileOutputStream(it, true) }?.bufferedWriter()
                            ?: System.out.bufferedWriter()).use { writer ->
                            writer.appendLine("benchmark")

                            val filesFlow = BenchmarkDataPreprocessor.parseBenchmarks(benchmarksDir)
                                .let { seq -> if (shuffled) seq.shuffled() else seq }
                                .let { seq -> if (benchmarksCountLimit > 0) seq.take(benchmarksCountLimit) else seq }
                                .asFlow()

                                .map { file ->
                                    scope.async(dispatcher) {
                                        val assertions = //parser //parsersPool.borrow { parser ->
                                            kotlin.runCatching {
                                                ctx.preprocessCoverageSamplerAssertions(file, parser)
                                            }
                                                /*}*/.onFailure { return@async null }.getOrThrow()

                                        val solver = solverManager.createSolver(ctx, KZ3Solver::class)
                                        val satisfiesTimeout = satisfiesCheckTimeoutAsync(
                                            assertions,
                                            KSolverStatus.SAT,
                                            solver,
                                            timeout = checkSatTimeout
                                        )
                                        solver.deleteSolverAsync()

                                        file to satisfiesTimeout
                                    }
                                }
                                .buffer(threadsCount)
                                .mapNotNull { def -> def.await() }
                                .filter { (file, sat) ->
                                    sat.also {
                                        if (!it) logger().trace { "Dropped out as unsatisfied timeout: $file" }
                                    }
                                }
                                .map { (file, _) -> file }
                                .let { flow -> if (benchmarksCountLimit > 0) flow.take(benchmarksCountLimit) else flow }
                                .onEach {
                                    logger().trace { it.absolutePath }
                                    writer.appendLine(it.absolutePath)
                                    writer.flush()
                                }
                                .flowOn(dispatcher)

                            filesFlow.collect()

                        }
                    }

                    dispatcher.close()
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

        suspend fun <S : KAsyncSolver<*>> satisfiesCheckTimeoutAsync(
            assertions: List<KExpr<KBoolSort>>,
            status: KSolverStatus = KSolverStatus.SAT,
            solver: S,
            timeout: Duration = INFINITE
        ): Boolean = solver.runCatching {
            pushAsync()
            assertions.forEach { solver.assertAsync(it) }

            val checkSatResult = checkAsync(timeout) == status
            solver.popAsync(1u)

            return@runCatching checkSatResult
        }
            .onFailure { logger().warn("Exception on check-sat: $it") }
            .getOrDefault(false)

        fun <S : KSolver<*>> satisfiesCheckTimeout(
            assertions: List<KExpr<KBoolSort>>,
            status: KSolverStatus = KSolverStatus.SAT,
            solver: S,
            timeout: Duration = 1.seconds
        ): Boolean {
            var satisfies = false
            try {
                solver.push()
                assertions.forEach(solver::assert)
                satisfies = solver.check(timeout) == status
                solver.pop()
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