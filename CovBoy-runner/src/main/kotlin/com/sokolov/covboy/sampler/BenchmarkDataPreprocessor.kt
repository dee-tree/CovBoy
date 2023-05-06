package com.sokolov.covboy.sampler

import com.sokolov.covboy.parseAssertions
import com.sokolov.covboy.process.ParserRunner
import com.sokolov.covboy.sampler.exceptions.UnsuitableFormulaCoverageSamplingException
import org.ksmt.KContext
import org.ksmt.expr.KExpr
import org.ksmt.runner.core.KsmtWorkerPool
import org.ksmt.runner.generated.models.TestProtocolModel
import org.ksmt.solver.KSolverStatus
import org.ksmt.sort.KBoolSort
import java.io.File
import kotlin.time.Duration

class BenchmarkDataPreprocessor {

    companion object {

        /**
         * @return assertions of the smtlib benchmark [benchmark], that are ready for CoverageSampler:
         *  * if formula is UNSAT, it returns negated assertions
         *  * if formula is SAT, it just returns original assertions
         *  * if formula is UNKNOWN or does not contain info line, it throws [UnsuitableFormulaCoverageSamplingException]
         *
         * Does not compute check-sat (just analyzes formula info :status line)
         */
        @JvmStatic
        fun preprocessCoverageSamplerAssertions(
            benchmark: File,
            ctx: KContext
        ): List<KExpr<KBoolSort>> {
            // in case of no satisfiability info in file -> throws [IllegalStateException]

            val status = parseStatusInfo(benchmark)

            if (status == KSolverStatus.UNKNOWN)
                throw UnsuitableFormulaCoverageSamplingException("UNKNOWN formula in benchmark file ${benchmark.absolutePath}")

            val assertions = ctx.parseAssertions(benchmark).let {
                if (status == KSolverStatus.UNSAT)
                    with(ctx) { listOf(!mkAnd(it)) }
                else it
            }

            return assertions
        }

        /**
         * @see preprocessCoverageSamplerAssertions
         */
        @JvmStatic
        suspend fun preprocessCoverageSamplerAssertionsAsync(
            benchmark: File,
            ctx: KContext,
            workersPool: KsmtWorkerPool<TestProtocolModel>,
            timeout: Duration
        ): List<KExpr<KBoolSort>> {
            // in case of no satisfiability info in file -> throws [IllegalStateException]

            val status = parseStatusInfo(benchmark)

            if (status == KSolverStatus.UNKNOWN)
                throw UnsuitableFormulaCoverageSamplingException("UNKNOWN formula in benchmark file ${benchmark.absolutePath}")

            val worker = workersPool.getOrCreateFreeWorker()
            worker.astSerializationCtx.initCtx(ctx)
            worker.lifetime.onTermination {
                worker.astSerializationCtx.resetCtx()
            }

            val parserRunner = ParserRunner(ctx, timeout, worker)
            val assertions: List<KExpr<KBoolSort>>
            try {
                parserRunner.init()
                assertions = parserRunner.parseSmtLibFile(benchmark.toPath()).let {
                    if (status == KSolverStatus.UNSAT)
                        with(ctx) { listOf(!mkAnd(it)) }
                    else it
                }
            } finally {
                parserRunner.delete()
                worker.release()
            }

            return assertions
        }

        /**
         * @return benchmarks with specific (set-info :status) status
         */
        @JvmStatic
        fun parseBenchmarks(
            benchmarksRootDir: File,
            filterOnStatusInfo: (KSolverStatus) -> Boolean = { it != KSolverStatus.UNKNOWN }
        ): Sequence<File> = benchmarksRootDir
            .walk()
            .filter(File::isFile)
            .filter { it.extension == "smt2" }
            .filter { filterOnStatusInfo(parseStatusInfo(it)) }

        /**
         * @return [KSolverStatus], specified in (set-info :status) line
         */
        @JvmStatic
        fun parseStatusInfo(smtBenchmark: File): KSolverStatus {
            val statusPrefix = "(set-info :status "

            val infoLine = smtBenchmark.useLines { lines ->
                lines.find { it.startsWith(statusPrefix) }
            } ?: return KSolverStatus.UNKNOWN

            return when {
                infoLine.startsWith("sat", statusPrefix.length) -> KSolverStatus.SAT
                infoLine.startsWith("unsat", statusPrefix.length) -> KSolverStatus.UNSAT
                else -> KSolverStatus.UNKNOWN
            }
        }
    }
}

fun KContext.preprocessCoverageSamplerAssertions(benchmark: File) =
    BenchmarkDataPreprocessor.preprocessCoverageSamplerAssertions(benchmark, this)

suspend fun KContext.preprocessCoverageSamplerAssertionsAsync(
    benchmark: File,
    workersPool: KsmtWorkerPool<TestProtocolModel>,
    timeout: Duration
) = BenchmarkDataPreprocessor.preprocessCoverageSamplerAssertionsAsync(benchmark, this, workersPool, timeout)
