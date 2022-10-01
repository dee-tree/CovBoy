package com.sokolov.smt.sampler

import com.microsoft.z3.coverage.CoverageSampler
import com.sokolov.smt.prover.IProver
import com.sokolov.smt.prover.Prover
import com.sokolov.smt.prover.SecondaryProver
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.sosy_lab.java_smt.SolverContextFactory
import org.sosy_lab.java_smt.SolverContextFactory.Solvers
import org.sosy_lab.java_smt.api.*
import java.io.File
import java.util.stream.Stream

abstract class CoverageSamplerTest {

    @ParameterizedTest
    @MethodSource("provideSmtInputPaths")
    open fun test(inputPath: String) {

        logger().info("input file: $inputPath")

        val context = SolverContextFactory.createSolverContext(Solvers.Z3)

        val cvc4Context = SolverContextFactory.createSolverContext(Solvers.CVC4)

        val prover = context.newProverEnvironment(
            SolverContext.ProverOptions.GENERATE_MODELS,
            SolverContext.ProverOptions.ENABLE_SEPARATION_LOGIC,
            SolverContext.ProverOptions.GENERATE_UNSAT_CORE
        ).let { Prover(it, context, File(inputPath)) }

        val cvc4Prover = cvc4Context.newProverEnvironment(
            SolverContext.ProverOptions.GENERATE_MODELS,
            SolverContext.ProverOptions.ENABLE_SEPARATION_LOGIC,
            SolverContext.ProverOptions.GENERATE_UNSAT_CORE
        )

        val cvc4ProverAsSecondary = SecondaryProver(
            cvc4Prover,
            cvc4Context,
            prover.constraints,
            prover
        )


//        val coverage = coverageSampler(context, prover).computeCoverage()
        val coverage = coverageSampler(cvc4Context, cvc4ProverAsSecondary).computeCoverage()
        println("coverage value for $inputPath: ${coverage.coverageNumber}")
        logger().debug("coverage.solverCheckCalls: ${coverage.solverCheckCalls}")
        logger().debug("Atoms count: ${coverage.atomsCount} (free: ${coverage.freeAtoms.size})")
        logger().debug("Free atoms portion: ${coverage.freeAtomsPortion}")
        logger().debug("Total constrained atoms: ${coverage.atomsCoverage.size}")
        logger().debug("total time coverage: ${coverage.coverageComputationMillis} ms")

    }

    abstract fun coverageSampler(context: SolverContext, prover: IProver): CoverageSampler


    companion object {
        @JvmStatic
        fun provideSmtInputPaths(): Stream<Arguments> =
            Stream.of(*(File("input").listFiles { file: File -> file.isFile && "simple" in file.name }?.map { Arguments.of(it.absolutePath) }
                ?: emptyList()).toTypedArray())
    }

}


