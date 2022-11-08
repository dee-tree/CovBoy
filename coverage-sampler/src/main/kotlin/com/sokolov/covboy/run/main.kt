package com.sokolov.covboy.run

import com.microsoft.z3.coverage.ModelsEnumerationCoverage
import com.microsoft.z3.coverage.intersections.ModelsIntersectionCoverage
import com.microsoft.z3.coverage.unsatcore.UnsatCoreBasedCoverageSampler
import com.sokolov.covboy.coverage.CoverageResultWrapper
import com.sokolov.covboy.coverage.CoverageSampler
import com.sokolov.covboy.coverage.provider.CoverageSamplerProvider
import com.sokolov.covboy.coverage.provider.EnumerationCoverageSamplerProvider
import com.sokolov.covboy.coverage.provider.IntersectionsCoverageSamplerProvider
import com.sokolov.covboy.coverage.provider.UnsatCoreBasedCoverageSamplerProvider
import com.sokolov.covboy.prover.BaseProverEnvironment
import com.sokolov.covboy.prover.Prover
import com.sokolov.covboy.prover.secondary.SecondaryProver
import com.sokolov.covboy.solvers.supportedTheories
import com.sokolov.covboy.solvers.theories
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.sosy_lab.common.ShutdownManager
import org.sosy_lab.common.configuration.Configuration
import org.sosy_lab.common.log.LogManager
import org.sosy_lab.java_smt.SolverContextFactory
import org.sosy_lab.java_smt.SolverContextFactory.Solvers
import org.sosy_lab.java_smt.api.SolverContext
import org.sosy_lab.java_smt.solvers.boolector.createExtendedBoolectorSolverContext
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
    val coverageProvider = when (args[3]) {
        ModelsIntersectionCoverage::class.simpleName -> IntersectionsCoverageSamplerProvider()
        ModelsEnumerationCoverage::class.simpleName -> EnumerationCoverageSamplerProvider()
        UnsatCoreBasedCoverageSampler::class.simpleName -> UnsatCoreBasedCoverageSamplerProvider()
        else -> error("unknown coverage way: ${args[3]}")
    }

    CoverageSamplerRunner(
        solver,
        coverageProvider,
        input,
        output
    ).run()

}

class CoverageSamplerRunner(
    val solver: Solvers,
    samplerProvider: CoverageSamplerProvider,
    val input: File,
    val output: File
) {

    private val prover = makeProver(solver, input)
    private val sampler = samplerProvider(prover)

    fun run() {
        val coverage = sampler.computeCoverage()

        output.writeText(Json.encodeToString(CoverageResultWrapper.fromCoverageResult(prover.solverName, coverage)))

        prover.close()
        prover.context.close()
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
    val prover = makeOriginProver(Solvers.Z3, input)

    if (prover.theories().any { it !in solver.supportedTheories }) {
        System.err.println("Prover $solver does not support ${prover.theories() - solver.supportedTheories} needed theories")
    }
    require(solver.supportedTheories.containsAll(prover.theories()))
}

fun checkCompatibility(origin: Solvers, other: Solvers, input: File) {
    val originProver = makeOriginProver(origin, input)

    if (originProver.theories().any { it !in other.supportedTheories }) {
        System.err.println("Prover $other does not support ${originProver.theories() - other.supportedTheories} needed theories")
    }
    require(other.supportedTheories.containsAll(originProver.theories()))
}

fun makeOriginProver(solver: Solvers, input: File): BaseProverEnvironment {
    val shutdownManager = ShutdownManager.create()
    val ctx = SolverContextFactory.createSolverContext(
        Configuration.defaultConfiguration(),
        LogManager.createNullLogManager(),
        shutdownManager.notifier, solver
    )
    val proverEnv = ctx.newProverEnvironment(
        SolverContext.ProverOptions.GENERATE_UNSAT_CORE,
        SolverContext.ProverOptions.GENERATE_MODELS
    )

    return Prover(proverEnv, ctx, input)
}

fun makeProver(solver: Solvers, input: File): BaseProverEnvironment {
    return if (solver == Solvers.Z3) {
        makeOriginProver(solver, input)
    } else {
        makeOtherProver(solver, makeOriginProver(Solvers.Z3, input))
    }
}

fun makeOtherProver(solver: Solvers, origin: BaseProverEnvironment): BaseProverEnvironment {
    val ctx = if (solver == Solvers.BOOLECTOR) createExtendedBoolectorSolverContext()
        else SolverContextFactory.createSolverContext(solver)
    return SecondaryProver(ctx, origin)
}