package com.sokolov.covboy.runner

import com.sokolov.covboy.bench.CoverageSamplerBenchmark
import com.sokolov.covboy.sampler.CoverageSamplerType
import org.ksmt.runner.generated.models.SolverType
import java.io.File
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun main(args: Array<String>) {
    val inputFormula = File(args[0])
    val solverType = SolverType.valueOf(args[1])

    val benchmark = CoverageSamplerBenchmark(
        inputFormula = inputFormula,
        solverType = solverType,
        samplerType = CoverageSamplerType.BaselinePredicatePropagating
    ).run().also { results ->
        val benchResult = results.first()

        println(benchResult)
    }
}