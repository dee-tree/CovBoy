package com.sokolov.covboy.runner

import com.sokolov.covboy.parseAssertions
import com.sokolov.covboy.predicates.bool.BoolPredicatesExtractor
import org.ksmt.KContext
import org.ksmt.solver.KSolverStatus
import org.ksmt.solver.z3.KZ3Solver
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class BenchmarksExtractor {

    companion object {

        fun main(args: Array<String>) {
            val benchmarksDir = File(args[0])
            val outFile = File(args[0])

            val writer = outFile.bufferedWriter()
            writer.appendLine("benchmark")

            BenchmarksExtractor.getSatBenchmarksRecursively(
                benchmarksDir,
                filter = { it.satisfiesSatTimeout(KSolverStatus.SAT, 1.seconds) },
                onEach = { println(it.absolutePath); writer.appendLine(it.absolutePath) }
            )

            writer.flush()
            return
        }

        @JvmStatic
        fun getBenchmarksWithMaxPredicatesCount(
            benchmarksRootDir: File,
            maxCount: Int = Int.MAX_VALUE
        ): List<File> = getSatBenchmarksRecursively(
            benchmarksRootDir,
        ).sortedByDescending { bench ->
            KContext(simplificationMode = KContext.SimplificationMode.NO_SIMPLIFY).use { ctx ->
                BoolPredicatesExtractor(ctx).extractPredicates(ctx.parseAssertions(bench)).size
            }
        }.take(maxCount)

        @JvmStatic
        fun getSatBenchmarksRecursively(
            benchmarksRootDir: File,
            filter: (File) -> Boolean = { true },
            onEach: (File) -> Unit = {},
            shuffled: Boolean = false,
            maxCount: Int = Int.MAX_VALUE,
        ): List<File> = benchmarksRootDir
            .walk()
            .filter { file: File ->
                file.isFile
                        && file.extension == "smt2"
                        && "(set-info :status sat)" in file.readText()
                        && filter(file)
            }
            .onEach(onEach)
            .let { if (shuffled) it.shuffled() else it }
            .take(maxCount)
            .toList()

        fun File.satisfiesSatTimeout(satStatus: KSolverStatus, timeout: Duration = 1.seconds): Boolean {
            var satisfies: Boolean = false
            try {
                satisfies = KContext(simplificationMode = KContext.SimplificationMode.NO_SIMPLIFY).use { ctx ->
                    KZ3Solver(ctx).use { solver ->
                        ctx.parseAssertions(this).forEach(solver::assert)
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
