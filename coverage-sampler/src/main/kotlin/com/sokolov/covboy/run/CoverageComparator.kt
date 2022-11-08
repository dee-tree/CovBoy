package com.sokolov.covboy.run

import com.sokolov.covboy.coverage.CoverageResultWrapper
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import org.sosy_lab.java_smt.SolverContextFactory.Solvers
import java.io.File

class CoverageComparator(val coverageFiles: List<File>) {

    private val coverages: List<CoverageResultWrapper> = coverageFiles.map {
        Json.decodeFromStream<CoverageResultWrapper>(it.inputStream())
    }


    fun compare(base: Solvers) {
        val baseCoverage = coverages.find { it.solver == base } ?: error("Base ($base) coverage not found")
        val others = coverages - baseCoverage

        println("Compare coverage in ${coverageFiles.first().parentFile.absolutePath}")

        others.forEach { other ->
            print("Compare ${baseCoverage.solver} VS ${other.solver}: ")

            if (baseCoverage.compareTo(other) == 0) {
                println("OK")
            } else {
                System.err.println("FAILED")
                System.err.println(CoverageResultWrapper.diffAsString(baseCoverage, other))

                assert(baseCoverage.compareTo(other) == 0) { "FAILED! Coverage for base $base is not same that for $other" }
            }
        }

        println("Compared ${others.size} solvers with base")
        println("-".repeat(30))
    }

    val solvers: List<Solvers>
        get() = coverages.map { it.solver }

    fun hasResult(solver: Solvers): Boolean = coverages.find { it.solver == solver } != null

    fun pairsCount(base: Solvers): Int = if (hasResult(base)) coverages.size - 1 else 0

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            // args[0] - root coverage results dir
            // args[1] - base solver
            val resultsDir = File(args[0])
            val baseSolver = Solvers.valueOf(args[1])

            var comparedBenchmarks = 0
            val solversBenchmarks = mutableMapOf<Solvers, Int>()

            resultsDir.walk()
                .filter { it.isDirectory && it != resultsDir }
                .forEach { covDir ->
                    val covFiles = covDir.listFiles { _, _ -> true }?.filter { it.extension == "json" } ?: emptyList()

                    val comparator = CoverageComparator(covFiles)

                    if (comparator.pairsCount(baseSolver) > 0) {
                        comparator.compare(baseSolver)
                        comparedBenchmarks++

                        comparator.solvers.forEach { solver ->
                            solversBenchmarks[solver] = solversBenchmarks.getOrDefault(solver, 0) + 1
                        }
                    }
                }

            println("Compared benchmarks: $comparedBenchmarks")
            println("Benchmarks for solvers: $solversBenchmarks")
        }
    }

}
