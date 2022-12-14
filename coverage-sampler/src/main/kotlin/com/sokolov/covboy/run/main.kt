package com.sokolov.covboy.run

import com.sokolov.covboy.coverage.CoverageResultWrapper
import com.sokolov.covboy.coverage.provider.CoverageSamplerProvider
import com.sokolov.covboy.coverage.provider.IntersectionsCoverageSamplerProvider
import com.sokolov.covboy.coverage.sampler.CoverageSampler
import com.sokolov.covboy.coverage.sampler.impl.ModelsIntersectionCoverageSampler
import com.sokolov.covboy.solvers.provers.provider.makePrimaryProver
import com.sokolov.covboy.solvers.provers.provider.makeProver
import com.sokolov.covboy.solvers.theories.supportedTheories
import com.sokolov.covboy.solvers.theories.theories
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.sosy_lab.java_smt.SolverContextFactory.Solvers
import java.io.File

fun main(args: Array<String>) {
    // args[0] - solver
    // args[1] - input
    // args[2] - outputFile
    // args[3] - coverage way
    println("Program ${ProcessHandle.current().pid()} is running with args: ${args.contentToString()}")

    val solver = Solvers.valueOf(args[0])
    val input = File(args[1])

    checkCompatibility(solver, input)

    val output = File(args[2])
    val errorFile = File(output.parentFile, output.nameWithoutExtension + "-error.json")

    val coverageProvider = when (args[3]) {
        ModelsIntersectionCoverageSampler::class.simpleName -> IntersectionsCoverageSamplerProvider()
//        ModelsEnumerationCoverage::class.simpleName -> EnumerationCoverageSamplerProvider()
//        UnsatCoreBasedCoverageSampler::class.simpleName -> UnsatCoreBasedCoverageSamplerProvider()
        else -> error("unknown coverage way: ${args[3]}")
    }

    CoverageSamplerRunner(
        solver,
        coverageProvider,
        input,
        output,
        errorFile
    ).run()

}

class CoverageSamplerRunner(
    val solver: Solvers,
    samplerProvider: CoverageSamplerProvider,
    val input: File,
    val output: File,
    val error: File
) {

    private val prover = makeProver(solver).apply { addConstraintsFromFile(input) }
    private val sampler = samplerProvider(prover)

    fun run() {
        try {
            val coverage = sampler.computeCoverage()
            output.writeText(Json.encodeToString(CoverageResultWrapper.fromCoverageResult(prover.solverName, coverage)))
        } catch (e: Exception) {
            SamplerCrash(SamplerCrash.Reasons.EXCEPTION, e.toString()).writeToFile(error)
        } finally {
            prover.close()
            prover.context.close()
        }
    }


    companion object {
        fun <T : CoverageSampler> makeMainArgs(
            solver: Solvers,
            input: File,
            output: File,
            coverageSampler: Class<T>
        ): List<String> = listOf(
            solver.name,
            input.absolutePath,
            output.absolutePath,
            coverageSampler.simpleName
        )
    }
}

fun checkCompatibility(solver: Solvers, input: File) {
    val prover = makePrimaryProver()
    prover.addConstraintsFromFile(input)

    if (prover.theories().any { it !in solver.supportedTheories }) {
        System.err.println("Prover $solver does not support ${prover.theories() - solver.supportedTheories} needed theories")
    }
    val neededTheories = prover.theories()
    prover.close()
    prover.context.close()
    require(solver.supportedTheories.containsAll(neededTheories))
}


