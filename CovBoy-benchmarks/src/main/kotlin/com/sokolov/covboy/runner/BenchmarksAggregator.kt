package com.sokolov.covboy.runner

import com.sokolov.covboy.data.SamplerBenchmarkData
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File
import java.io.FileOutputStream

class BenchmarksAggregator {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val measurementsPath = File(args[0])



            measurementsPath.listFiles { file -> file.extension == "json" }!!
                .map { Json.decodeFromStream<List<SamplerBenchmarkData>>(it.inputStream()) }
                .flatten()
                .first().also { benchData ->
                    benchData.writeCsv(
                        FileOutputStream("/ssd/sokolov/IdeaProjects/CovBoy/CovBoy-benchmarks/data/aaa.csv")
                    )
                }
        }
    }
}