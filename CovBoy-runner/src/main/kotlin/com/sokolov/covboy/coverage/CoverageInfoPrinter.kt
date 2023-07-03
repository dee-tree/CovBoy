package com.sokolov.covboy.coverage

import org.ksmt.KContext
import org.ksmt.runner.generated.models.SolverType
import org.ksmt.sort.KBoolSort
import java.io.File
import java.util.*

class CoverageInfoPrinter {
    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val argFile = File(args[0])

            if (argFile.isDirectory)
                printTotalCoverageInfo(argFile)
            else
                printCoverageInfo(argFile)
        }

        @JvmStatic
        fun printTotalCoverageInfo(
            coverageDir: File
        ) {
            val coverageCases = PredicatesCoverageComparatorRunner.getCoverageFileCases(coverageDir)

            val errorsCountBySolverType = hashMapOf<SolverType, Int>().also { map ->
                SolverType.values().forEach { map[it] = 0 }
            }
            val successfulCountBySolverType = hashMapOf<SolverType, Int>().also { map ->
                SolverType.values().forEach { map[it] = 0 }
            }
            val errorsCountByReasons = hashMapOf<PredicatesCoverageSamplingError.Reasons, Int>().also { map ->
                PredicatesCoverageSamplingError.Reasons.values().forEach { map[it] = 0 }
            }

            val errorsCountBySolverTypeAndReason =
                hashMapOf<SolverType, MutableMap<PredicatesCoverageSamplingError.Reasons, Int>>().also { m ->
                    SolverType.values().forEach { solverType ->
                        m[solverType] = EnumMap(PredicatesCoverageSamplingError.Reasons::class.java)
                        m.getValue(solverType).also { mm ->
                            PredicatesCoverageSamplingError.Reasons.values().forEach { reason ->
                                mm[reason] = 0
                            }
                        }
                    }
                }

            val unknownFiles = hashSetOf<File>()
//            val timeouts = hashMapOf<Map<SolverType, File>, Int>()

            coverageCases.forEach { coverageCase ->
                coverageCase.forEach { (solverType, coverageFile) ->
                    KContext(simplificationMode = KContext.SimplificationMode.NO_SIMPLIFY).use { ctx ->
                        val serializer = PredicatesCoverageSerializer(ctx)
                        val inputStream = coverageFile.inputStream().buffered()
                        if (serializer.isCompleteCoverage(inputStream)) {
                            successfulCountBySolverType[solverType] =
                                successfulCountBySolverType.getValue(solverType) + 1
                        } else {
                            errorsCountBySolverType[solverType] = errorsCountBySolverType.getValue(solverType) + 1
                            val coverageError = serializer.deserializeError(inputStream)

                            if (coverageError.reason == PredicatesCoverageSamplingError.Reasons.UnknownDuringSampling) {
                                unknownFiles += coverageFile
                            }

                            if (coverageError.reason != PredicatesCoverageSamplingError.Reasons.UnknownDuringSampling && coverageError.reason != PredicatesCoverageSamplingError.Reasons.InitiallyUnsuitableFormulaSatisfiability
                                && coverageError.reason == PredicatesCoverageSamplingError.Reasons.TimeoutExceeded) {
//                                timeouts[coverageCase] = timeouts.getOrDefault(coverageCase, 0) + 1
                                1 + 3;
                            }
                            errorsCountByReasons[coverageError.reason] =
                                errorsCountByReasons.getValue(coverageError.reason) + 1
                            errorsCountBySolverTypeAndReason.getValue(solverType)[coverageError.reason] =
                                errorsCountBySolverTypeAndReason.getValue(solverType).getValue(coverageError.reason) + 1

                        }

                    }
                }
            }

            println("Successful covered:\n${
                successfulCountBySolverType.entries.joinToString("\n") { (solver, count) ->
                    "\t$solver: $count"
                }
            }")

            println("Errors totally (solvers):\n${
                errorsCountBySolverType.entries.joinToString("\n") { (solver, count) ->
                    "\t$solver: $count"
                }
            }")

            println("Errors totally (reasons):\n${
                errorsCountByReasons.entries.joinToString("\n") { (reason, count) ->
                    "\t$reason: $count"
                }
            }")

            println(
                "Error by reasons:\n${
                    errorsCountBySolverTypeAndReason.entries.joinToString("\n") { (solver, errors) ->
                        "$solver:\n\t" + errors.entries.joinToString("\n\t") { (reason, count) -> "$reason: $count" }
                    }
                }"
            )

        }

        @JvmStatic
        fun printCoverageInfo(
            coverageFile: File
        ) {
            val ctx = KContext(simplificationMode = KContext.SimplificationMode.NO_SIMPLIFY)
            val serializer = PredicatesCoverageSerializer(ctx)

            println("Coverage info on $coverageFile ---")
            println()

            print("Coverage completeness: ")

            if (serializer.isCompleteCoverage(coverageFile.inputStream().buffered())) {
                println("Success")

                val coverage = serializer.deserialize<KBoolSort>(coverageFile.inputStream())
                println("\t* SolverType: ${coverage.solverType}")
                println(
                    "\t* Fully covered predicates count: ${
                        coverage.coverageSat.count { (predicate, value) ->
                            coverage.isCovered(predicate)
                        }
                    }"
                )

            } else {
                println("Error during sampling")

                val coverageError = serializer.deserializeError(coverageFile.inputStream())
                println("\t* SolverType: ${coverageError.solverType}")
                println("\t* Reason of error: ${coverageError.reason}")
                println("\t* Error message: ${coverageError.text.ifBlank { "Empty message" }}")

            }

            println("\t${"-".repeat(20)}")
        }
    }
}