package com.sokolov.covboy.sampler.benchmarks

import java.io.File

class BenchmarksSelector {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val benchmarksDir = File(args[0])
            val maxBenchmarksCount = args[1].toInt()

            println("Benchmarks: ")

            getSatBenchmarksRecursively(benchmarksDir, maxCount = maxBenchmarksCount, shuffled = true)
                .forEach { benchmark ->
                    println(benchmark.absolutePath)
                    //benchmark.copyTo(File("/ssd/sokolov/IdeaProjects/CovBoy/CovBoy-benchmarks/data/benchmarks", benchmark.name))
                }
        }

        @JvmStatic
        fun getSatBenchmarksRecursively(
            benchmarksRootDir: File,
            filter: (File) -> Boolean = { true },
            shuffled: Boolean = false,
            maxCount: Int = Int.MAX_VALUE
        ): List<File> = benchmarksRootDir
            .walk()
            .filter { file: File ->
                file.isFile
                        && file.extension == "smt2"
                        && "(set-info :status unsat)" !in file.readText()
                        && filter(file)
            }
            .let { if (shuffled) it.shuffled() else it }
            .take(maxCount)
            .toList()
    }
}