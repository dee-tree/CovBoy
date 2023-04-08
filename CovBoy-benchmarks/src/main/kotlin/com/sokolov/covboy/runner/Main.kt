package com.sokolov.covboy.runner

import com.sokolov.covboy.bench.CoverageSamplerBenchmark
import com.sokolov.covboy.sampler.CoverageSamplerType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import org.ksmt.runner.generated.models.SolverType
import java.io.File
import kotlin.time.ExperimentalTime

@ExperimentalTime
fun main(args: Array<String>) {
    val inputFile = File(args[0])
    val solverType = SolverType.valueOf(args[1])
    val outputDir = File(args[2])

    val inputBenchmarks = if (inputFile.isDirectory) inputFile.listFiles() else arrayOf(inputFile)

    inputBenchmarks.forEach { inputBench ->
        println("Run benchmark on $inputBench")
        val bench = CoverageSamplerBenchmark(
            inputFormula = inputBench,
            solverType = solverType,
            samplerType = CoverageSamplerType.BaselinePredicatePropagating,
        )

        bench.run().also { results ->
            val outFile = File(outputDir, "bench-${inputBench.nameWithoutExtension}.json")
            outFile.createNewFile()
            Json.encodeToStream(results, outFile.outputStream())
        }
    }
}