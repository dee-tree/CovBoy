package com.sokolov.covboy.run

import com.sokolov.covboy.logger
import com.sokolov.covboy.solvers.provers.provider.makeProver
import org.sosy_lab.java_smt.SolverContextFactory.Solvers
import java.io.File

class CoverageMismatchesFinder(val rootBenchmarksDir: File, val benchmarkPrefix: String, val solvers: List<Solvers>) {

    private fun benchmarkIfFailed(benchmark: File): File? {
        logger().trace("Compare satisfiability on benchmark [$benchmark]")
        val partialName = benchmark.nameWithoutExtension.split("-")

        val firstSolver = Solvers.valueOf(partialName[partialName.size - 2])
        val secondSolver = Solvers.valueOf(partialName.last())

        val firstProver = makeProver(firstSolver).apply { addConstraintsFromFile(benchmark) }
        val secondProver = makeProver(secondSolver).apply { addConstraintsFromFile(benchmark) }

        val firstResult = firstProver.checkSat()
        val secondResult = secondProver.checkSat()

        val results = setOf(firstResult, secondResult)

        firstProver.close()
        secondProver.close()

        if (results.size > 1) logger().warn("Different (check-sat) on benchmark [$benchmark]: $firstSolver - $firstResult, $secondSolver - $secondResult")
        return if (results.size > 1) benchmark else null
    }

    fun findFirst(): File? {
        rootBenchmarksDir.walk()
            .filter { it.name.startsWith(benchmarkPrefix) }
            .forEach { benchmark ->
                val res = benchmarkIfFailed(benchmark)
                res?.let { return it } ?: benchmark.delete()
            }
        return null
    }

    fun findAll(): List<File> {
        return rootBenchmarksDir.walk()
            .filter { it.name.startsWith(benchmarkPrefix) }
            .mapNotNull { benchmark ->
                benchmarkIfFailed(benchmark) ?: run { benchmark.delete(); null }
            }.toList()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            // args[0] - root mutated benchmarks path
            // args[1] - benchmark prefix
            // args[2] - stop after first failure - fast stop(true/false)
            // args[3+] - solvers

            val rootBenchmarksDir = File(args[0])
            val benchmarkPrefix = args[1]
            val fastStop = args[2].toBoolean()
            val solvers = if (args.size == 4 && args[3] == "all")
                listOf(Solvers.Z3, Solvers.BOOLECTOR, Solvers.SMTINTERPOL, Solvers.CVC4, Solvers.PRINCESS)
            else
                List(args.size - 3) { Solvers.valueOf(args[3 + it]) }

            val mf = CoverageMismatchesFinder(rootBenchmarksDir, benchmarkPrefix, solvers)
            val buggyFiles = if (fastStop)
                mf.findFirst()?.let { listOf(it) } ?: emptyList()
            else mf.findAll()

            if (buggyFiles.isEmpty()) {
                println("All files are OK. No difference in behaviour found")
            } else {
                System.err.println("Found different behaviour on there files:")
                System.err.println(buggyFiles.joinToString("\n"))
            }

        }
    }
}