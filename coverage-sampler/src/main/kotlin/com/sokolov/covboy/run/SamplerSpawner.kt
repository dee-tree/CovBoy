package com.sokolov.covboy.run

import com.microsoft.z3.coverage.ModelsEnumerationCoverage
import com.microsoft.z3.coverage.intersections.ModelsIntersectionCoverage
import com.microsoft.z3.coverage.unsatcore.UnsatCoreBasedCoverageSampler
import com.sokolov.covboy.coverage.CoverageSampler
import com.sokolov.covboy.logger
import kotlinx.coroutines.*
import org.sosy_lab.java_smt.SolverContextFactory.Solvers
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

class SamplerSpawner<T : CoverageSampler>(
    val inputsRootDir: File,
    val outputsRootDir: File,
    val solvers: List<Solvers>,
    val timeout: Pair<Long, TimeUnit> = 60L to TimeUnit.SECONDS,
    val coverageSampler: Class<T>
) {

    fun runProcesses(rewriteOldResults: Boolean = false) {
        val classpath = System.getProperty("java.class.path")
        val main = "com.sokolov.covboy.run.MainKt"

        val inputs = getInputFormulaPaths()
        inputs.forEachIndexed { index, input ->
            logger().info("Collect coverage [$index / ${inputs.size}] on file [$input]")

            val thisInputSolvers = solvers.filter {
                val outFile = getOutFile(input.absolutePath, it)
                val errorFile = File(outFile.parentFile, outFile.nameWithoutExtension + "-error.json")

                rewriteOldResults || (!outFile.exists() || outFile.length() == 0L) && (!errorFile.exists() || errorFile.length() == 0L)
            }

            val commands = thisInputSolvers
                .map { solver ->
                    val outFile = getOutFile(input.absolutePath, solver)
                    val errorFile = File(outFile.parentFile, outFile.nameWithoutExtension + "-error.json")

                val args = CoverageSamplerRunner.makeMainArgs(
                    solver,
                    input,
                    outFile,
                    coverageSampler
                )

                val command = listOf("java", "-classpath", classpath, main) + args

                command to errorFile
            }

            measureTimeMillis {
                runBlocking {

                    val processes = commands.map {
                        it.first.runInProcess() to it.second
                    }

                    val processesDef = processes.map {
                        async(Dispatchers.IO) {
                            val finished = it.first.waitFor(timeout.first, timeout.second)
                            if (!finished) {
                                logger().warn("Kill process $it on proceeded timeout ($timeout)")
                                it.first.destroyForcibly()
                                SamplerCrash(SamplerCrash.Reasons.TIMEOUT, SamplerTimeOutException(timeout).toString()).writeToFile(it.second)
                            }
                        }
                    }

                    processesDef.awaitAll()
                }
            }.also {
                logger().info("Coverage of file [$input] collected | $it ms")
                logger().info("Ran on solvers: $thisInputSolvers")
                logger().info("-".repeat(30))
            }
        }

    }

    private fun getInputFormulaPaths(filter: (File) -> Boolean = { true }): List<File> = inputsRootDir
        .walk()
        .filter { file: File ->
            file.isFile
                    && file.extension == "smt2"
                    && "(set-info :status unsat)" !in file.readText()
        }
        .toList()
        .filter(filter)

    private fun getOutFile(inputFilePath: String, solver: Solvers): File {
        val input = File(inputFilePath)
        val resultDir = File(File(outputsRootDir, input.parentFile.relativeTo(inputsRootDir).path), input.nameWithoutExtension)
        resultDir.mkdirs()

        return File(resultDir, "$solver.json")
    }


    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            // args[0] - input path
            // args[1] - output path
            // args[2] - coverage sampler
            // args[3+] - solvers

            val input = File(args[0])
            val output = File(args[1])
            val sampler = when (args[2]) {
                ModelsIntersectionCoverage::class.simpleName -> ModelsIntersectionCoverage::class.java
                ModelsEnumerationCoverage::class.simpleName -> ModelsEnumerationCoverage::class.java
                UnsatCoreBasedCoverageSampler::class.simpleName -> UnsatCoreBasedCoverageSampler::class.java
                else -> error("Unknown coverage sampler: ${args[2]}")
            }
            val solvers = if (args.size == 4 && args[3] == "all")
                listOf(Solvers.Z3, Solvers.BOOLECTOR, Solvers.SMTINTERPOL, Solvers.CVC4, Solvers.PRINCESS)
            else
                List(args.size - 3) { Solvers.valueOf(args[3 + it]) }

            SamplerSpawner(
                coverageSampler = sampler,
                inputsRootDir = input,
                outputsRootDir = output,
                solvers = solvers
            ).runProcesses()
        }
    }
}

fun List<String>.runInProcess(): Process = ProcessBuilder().command(this).inheritIO().start()
