package com.sokolov.covboy.runner

import com.sokolov.covboy.parseAssertions
import com.sokolov.covboy.predicates.bool.BoolPredicatesExtractor
import org.ksmt.KContext
import java.io.File

class BenchmarksExtractor {

    companion object {

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
            shuffled: Boolean = false,
            maxCount: Int = Int.MAX_VALUE
        ): List<File> = benchmarksRootDir
            .walk()
            .filter { file: File ->
                file.isFile
                        && file.extension == "smt2"
                        && "(set-info :status sat)" in file.readText()
                        && filter(file)
            }
            .let { if (shuffled) it.shuffled() else it }
            .take(maxCount)
            .toList()
    }
}
