package com.sokolov.covboy.coverage.runner

import com.sokolov.covboy.coverage.runner.error.SamplerCrashInfo
import com.sokolov.covboy.coverage.runner.error.SamplerTimeOutException
import com.sokolov.covboy.coverage.sampler.CoverageSampler
import com.sokolov.covboy.coverage.sampler.impl.GroupingModelsCoverageSampler
import com.sokolov.covboy.utils.logger
import com.sokolov.covboy.utils.solverName
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.ksmt.solver.bitwuzla.KBitwuzlaSolver
import org.ksmt.solver.z3.KZ3Solver
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass
import kotlin.system.measureTimeMillis

class CoverageSamplerSpawner<T : CoverageSampler<*>>(
    val inputsRootDir: File,
    val outputsRootDir: File,
    val solvers: List<String>,
    val timeout: Pair<Long, TimeUnit> = 60L to TimeUnit.SECONDS,
    val coverageSampler: KClass<T>
) {


    fun runProcesses(rewriteOldResults: Boolean = false) {
        val classpath = System.getProperty("java.class.path")
        val main = "com.sokolov.covboy.coverage.runner.MainKt"

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

                    val args = makeMainArgs(
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

                    val dispatcher = Executors.newFixedThreadPool(processes.size).asCoroutineDispatcher()

                    val processesDef = processes.map {

                        async(dispatcher) {
                            val finished = it.first.waitFor(timeout.first, timeout.second)
                            if (!finished) {
                                logger().warn("Kill process $it on proceeded timeout ($timeout)")
                                it.first.destroyForcibly()
                                SamplerCrashInfo(SamplerCrashInfo.Reasons.TIMEOUT, SamplerTimeOutException(timeout).toString()).writeToFile(it.second)
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

    private fun getOutFile(inputFilePath: String, solver: String): File {
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
            // args[2+] - solvers

            val input = File(args[0])
            val output = File(args[1])
            val solvers = if (args.size == 3 && args[2] == "all")
                listOf(KZ3Solver::class.solverName, KBitwuzlaSolver::class.solverName)
            else
                List(args.size - 2) { args[2 + it] }

            CoverageSamplerSpawner(
                coverageSampler = GroupingModelsCoverageSampler::class,
                inputsRootDir = input,
                outputsRootDir = output,
                solvers = solvers
            ).runProcesses()
        }
    }
}

fun List<String>.runInProcess(): Process = ProcessBuilder().command(this).inheritIO().start()