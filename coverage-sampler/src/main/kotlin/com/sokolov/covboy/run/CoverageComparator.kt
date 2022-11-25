package com.sokolov.covboy.run

import com.sokolov.covboy.coverage.CoverageResultWrapper
import com.sokolov.covboy.coverage.provider.IntersectionsCoverageSamplerProvider
import com.sokolov.covboy.run.CoverageComparator.CompareResult.Companion.compare
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.sosy_lab.java_smt.SolverContextFactory.Solvers
import java.io.File

class CoverageComparator(val coverageFiles: List<File>) {

    enum class CompareStatus {
        OK, FAILURE
    }

    data class CompareResult(
        val file: File,
        val base: Solvers,
        val solver: Solvers,
        val status: CompareStatus,

        ) {
        companion object {
            fun Pair<CoverageResultWrapper, CoverageResultWrapper>.compare(file: File): CompareResult {
                val cmpResult = first.compareTo(second)

                return CompareResult(
                    file,
                    first.solver,
                    second.solver,
                    if (cmpResult == 0) CompareStatus.OK else CompareStatus.FAILURE
                )
            }
        }

        fun asLines(): String = """
            File: $file
            Compare $base VS $solver
            Status: $status
            ${"-".repeat(30)}
        """.trimIndent()
    }

    private val coverages: List<CoverageResultWrapper> = coverageFiles.map {
        Json.decodeFromStream<CoverageResultWrapper>(it.inputStream())
    }


    fun compare(base: Solvers): List<CompareResult> {
        val baseCoverage = coverages.find { it.solver == base } ?: error("Base ($base) coverage not found")
        val others = coverages - baseCoverage

        val result = others.map { other ->

            (baseCoverage to other).compare(coverageFiles.first().parentFile.absoluteFile)
        }

        return result
    }

    val solvers: List<Solvers>
        get() = coverages.map { it.solver }

    fun hasResult(solver: Solvers): Boolean = coverages.find { it.solver == solver } != null

    fun pairsCount(base: Solvers): Int = if (hasResult(base)) coverages.size - 1 else 0

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            // args[0] - root coverage results dir
            // args[1] - root coverage inputs dir
            // args[2] - base solver
            // args[3] - stop after first failure - fast stop(true/false)
            // args[4] - rewrite dumped formulas
            val inputsDir = File(args[0])
            val resultsDir = File(args[1])
            val baseSolver = Solvers.valueOf(args[2])
            val fastStop = args[3].toBoolean()
            val rewriteOldResults = args[4].toBoolean()

            var totalBenchmarks = 0
            var comparedBenchmarks = 0
            val solversBenchmarks = mutableMapOf<Solvers, Int>()
            val errorCases = mutableMapOf<Solvers, Int>()

            val solversNames = Solvers.values().map { it.name }
            run breaking@{
                resultsDir.walk()
                    .filter { it.isDirectory }
                    .forEach { covDir ->
                        val covFiles = covDir.listFiles { _, _ -> true }
                            ?.filter { it.extension == "json" && it.nameWithoutExtension in solversNames }
                            ?: emptyList()

                        val comparator = CoverageComparator(covFiles)

                        totalBenchmarks++
                        if (comparator.pairsCount(baseSolver) > 0) {
                            val result = comparator.compare(baseSolver)

                            comparedBenchmarks++

                            comparator.solvers.forEach { solver ->
                                solversBenchmarks[solver] = solversBenchmarks.getOrDefault(solver, 0) + 1
                            }

                            if (result.any { it.status == CompareStatus.FAILURE }) {
                                result.filter { it.solver != Solvers.PRINCESS }.forEach {
                                    if (it.status == CompareStatus.OK)
                                        println(it.asLines())
                                    else {
                                        println("\u001b[31m" + it.asLines() + "\u001b[0m")

                                        errorCases[it.solver] = errorCases.getOrDefault(it.solver, 0) + 1

                                        val inputFile = File(inputsDir, covDir.relativeTo(resultsDir).path + ".smt2")

                                        val diffAnalyzer = CoverageDiffDumper(inputFile, baseSolver, it.solver, IntersectionsCoverageSamplerProvider())
                                        diffAnalyzer.dumpToFile("cov-mismatch", covDir, rewriteOldResults)
                                    }
                                }
                                println()

                                if (fastStop) return@breaking
                            }
                        }
                    }
            }

            println("Total benchmarks: $totalBenchmarks")
            println("Compared benchmarks: $comparedBenchmarks")
            println("Benchmarks for solvers: $solversBenchmarks")
            println("Deviation cases: $errorCases")
        }
    }

}
