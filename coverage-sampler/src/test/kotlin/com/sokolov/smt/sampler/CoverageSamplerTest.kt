package com.sokolov.smt.sampler

import com.sokolov.covboy.coverage.CoverageSampler
import com.sokolov.covboy.logger
import com.sokolov.covboy.prover.BaseProverEnvironment
import com.sokolov.covboy.prover.Prover
import com.sokolov.covboy.prover.SecondaryProver
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.sosy_lab.common.ShutdownManager
import org.sosy_lab.common.configuration.Configuration
import org.sosy_lab.common.log.LogManager
import org.sosy_lab.java_smt.SolverContextFactory
import org.sosy_lab.java_smt.SolverContextFactory.Solvers
import org.sosy_lab.java_smt.api.*
import java.io.File
import java.util.stream.Stream


@Deprecated("use CoverageEstimator")
abstract class CoverageSamplerTest {

    @ParameterizedTest
    @MethodSource("provideSmtInputPaths")
    open fun test(inputPath: String) {

        logger().info("input file: $inputPath")

        val primaryShutdownManager = ShutdownManager.create()
        val primaryContext = SolverContextFactory.createSolverContext(
            Configuration.defaultConfiguration(),
            LogManager.createNullLogManager(),
            primaryShutdownManager.notifier,
            Solvers.Z3
        )

        val primaryProver = primaryContext.newProverEnvironment(
            SolverContext.ProverOptions.GENERATE_MODELS,
            SolverContext.ProverOptions.ENABLE_SEPARATION_LOGIC,
            SolverContext.ProverOptions.GENERATE_UNSAT_CORE,
        ).let { Prover(it, primaryContext, File(inputPath), primaryShutdownManager) }

        val shutdownManager = ShutdownManager.create()
        val context = SolverContextFactory.createSolverContext(
            Configuration.defaultConfiguration(),
            LogManager.createNullLogManager(),
            shutdownManager.notifier,
            Solvers.CVC4
        )

        val prover = context.newProverEnvironment(
            SolverContext.ProverOptions.GENERATE_MODELS,
            SolverContext.ProverOptions.ENABLE_SEPARATION_LOGIC,
            SolverContext.ProverOptions.GENERATE_UNSAT_CORE,
        ).let { SecondaryProver(context, primaryProver, shutdownManager) }

        val coverage = coverageSampler(prover).computeCoverage()
        println("coverage value for $inputPath: ${coverage.coverageNumber}")
        logger().debug("coverage.solverCheckCalls: ${coverage.solverCheckCalls}")
        logger().debug("Atoms count: ${coverage.atomsCount} (free: ${coverage.freeAtoms.size})")
        logger().debug("Free atoms portion: ${coverage.freeAtomsPortion}")
        logger().debug("Total constrained atoms: ${coverage.atomsCoverage.size}")
        logger().debug("total time coverage: ${coverage.coverageComputationMillis} ms")

    }

    abstract fun coverageSampler(prover: BaseProverEnvironment): CoverageSampler


    companion object {
        @JvmStatic
        fun provideSmtInputPaths(): Stream<Arguments> =
            Stream.of(*(File("input").listFiles { file: File -> file.isFile && "bug2-15" in file.name }?.map { Arguments.of(it.absolutePath) }
                ?: emptyList()).toTypedArray())
    }

}


