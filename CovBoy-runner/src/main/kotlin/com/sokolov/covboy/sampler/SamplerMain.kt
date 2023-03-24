package com.sokolov.covboy.sampler

import com.sokolov.covboy.UnknownSolverStatusOnCoverageSamplingException
import com.sokolov.covboy.coverage.PredicatesCoverageSamplingError
import com.sokolov.covboy.parseAssertions
import com.sokolov.covboy.predicates.bool.BoolPredicatesExtractor
import com.sokolov.covboy.predicates.bool.mkBoolPredicatesUniverse
import com.sokolov.covboy.sampler.impl.UncoveredPredicatesPropagatingCoverageSampler
import org.ksmt.KContext
import org.ksmt.runner.generated.models.SolverType
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class SamplerMain {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val solverType = SolverType.valueOf(args[0])
            val inSmtLibFormula = File(args[1])
            val outCoverageFile = File(args[2])
            val solverTimeoutMillis = args[3].toLong()
            val solverTimeout = solverTimeoutMillis.milliseconds

            runSamplerSmtLib(
                solverType,
                inSmtLibFormula,
                outCoverageFile,
                solverTimeout
            )
        }

        fun makeArgs(
            solverType: SolverType,
            inSmtLibFormula: File,
            outCoverageFile: File,
            solverTimeout: Duration = 1.seconds
        ): Array<String> = arrayOf(
            solverType.name,
            inSmtLibFormula.absolutePath,
            outCoverageFile.absolutePath,
            solverTimeout.inWholeMilliseconds.toString()
        )

        @JvmStatic
        fun runSamplerSmtLib(
            solverType: SolverType,
            smtLibFormulaFile: File,
            outCoverageFile: File,
            solverTimeout: Duration = 1.seconds
        ) {
            outCoverageFile.parentFile.mkdirs()

            KContext().use { ctx ->

                val assertions = ctx.parseAssertions(smtLibFormulaFile)

                // TODO: extend with int predicates
                val predicates = BoolPredicatesExtractor(ctx).extractPredicates(assertions)

                val sampler = UncoveredPredicatesPropagatingCoverageSampler(
                    solverType,
                    ctx,
                    assertions,
                    ctx.mkBoolPredicatesUniverse(),
                    predicates,
                    solverTimeout = solverTimeout
                )

                try {
                    val coverage = sampler.computeCoverage()
                    coverage.serialize(ctx, outCoverageFile.outputStream())
                } catch (e: UnknownSolverStatusOnCoverageSamplingException) {
                    PredicatesCoverageSamplingError(
                        PredicatesCoverageSamplingError.Reasons.UnknownDuringSampling,
                        e.stackTraceToString(),
                        solverType
                    ).serialize(
                        ctx,
                        outCoverageFile.outputStream()
                    )
                } catch (e: Exception) {
                    PredicatesCoverageSamplingError(
                        PredicatesCoverageSamplingError.Reasons.Other,
                        e.stackTraceToString(),
                        solverType
                    ).serialize(
                        ctx,
                        outCoverageFile.outputStream()
                    )
                }
            }

        }
    }
}