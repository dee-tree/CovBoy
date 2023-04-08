package com.sokolov.covboy.coverage

import com.sokolov.covboy.logger
import org.ksmt.KContext
import org.ksmt.runner.generated.models.SolverType
import java.io.File
import kotlin.system.exitProcess

class PredicatesCoverageComparatorRunner {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val rootCoverageDir = File(args[0])
            val primarySolver = SolverType.valueOf(args[1])

            runBenchmarksCoverageComparator(
                rootCoverageDir,
                primarySolver
            )
        }


        @JvmStatic
        fun runBenchmarksCoverageComparator(
            rootCoverageDir: File,
            primarySolver: SolverType
        ) {
            val coverageCases = getCoverageFileCases(rootCoverageDir)

            for (coverageCase in coverageCases) {
                if (primarySolver !in coverageCase) continue

                val ctx = KContext(simplificationMode = KContext.SimplificationMode.NO_SIMPLIFY)

                (coverageCase.keys - primarySolver).forEach { secondarySolver ->
                    CoverageComparator.compare(
                        coverageCase[primarySolver]!!.inputStream(),
                        coverageCase[secondarySolver]!!.inputStream(),
                        ctx
                    ) { status ->

                        val coverageDirPath = coverageCase[primarySolver]!!.parentFile.absolutePath
                        when (status) {
                            CoverageComparator.CoverageCompareStatus.EQUAL -> {
                                logger().info("Coverage equal: [$primarySolver - $secondarySolver] on $coverageDirPath")
                            }

                            CoverageComparator.CoverageCompareStatus.UNEQUAL -> {
                                logger().error("Different coverage: [$primarySolver - $secondarySolver] on file: $coverageDirPath")
                                exitProcess(11)
                            }

                            CoverageComparator.CoverageCompareStatus.SAMPLING_ERROR -> {
                                logger().warn("Coverage sampling error: skip [$primarySolver - $secondarySolver] at $coverageDirPath")
                            }
                        }

                    }
                }
            }

            logger().info("Totally compared coverage of ${coverageCases.size} formulas")
        }

        @JvmStatic
        private fun getCoverageDirsRecursively(
            rootCoverageDir: File
        ): List<File> = rootCoverageDir.walk()
            .filter { file: File ->
                file.isDirectory
            }
            .toList()


        @JvmStatic
        private fun getCoverageCase(dir: File): Map<SolverType, File> = dir.listFiles { _, _ -> true }
            ?.filter { it.extension == "cov" }?.associate {
                SolverType.valueOf(it.nameWithoutExtension) to it
            }
            ?: emptyMap()

        private fun getCoverageFileCases(rootCoverageDir: File): List<Map<SolverType, File>> =
            getCoverageDirsRecursively(rootCoverageDir).map {
                getCoverageCase(it)
            }
    }
}