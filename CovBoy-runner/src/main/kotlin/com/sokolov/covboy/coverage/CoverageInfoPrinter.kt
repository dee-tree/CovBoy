package com.sokolov.covboy.coverage

import org.ksmt.KContext
import org.ksmt.sort.KBoolSort
import java.io.File

class CoverageInfoPrinter {
    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            printCoverageInfo(File(args[0]))
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