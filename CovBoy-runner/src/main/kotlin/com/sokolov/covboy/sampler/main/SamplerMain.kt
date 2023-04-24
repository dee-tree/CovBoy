package com.sokolov.covboy.sampler.main

import com.sokolov.covboy.coverage.PredicatesCoverageSamplingError
import com.sokolov.covboy.predicates.bool.BoolPredicatesExtractor
import com.sokolov.covboy.predicates.bool.mkBoolPredicatesUniverse
import com.sokolov.covboy.sampler.*
import com.sokolov.covboy.sampler.exceptions.UnknownSolverStatusOnCoverageSamplingException
import com.sokolov.covboy.sampler.exceptions.UnsuitableFormulaCoverageSamplingException
import com.sokolov.covboy.sampler.impl.getModelsGroupSizeParam
import com.sokolov.covboy.sampler.impl.hasModelsGroupSizeParam
import com.sokolov.covboy.sampler.params.CoverageSamplerParams
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import org.ksmt.KContext
import org.ksmt.runner.generated.models.SolverType
import java.io.File

class SamplerMain {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) = mainBody {
            ArgParser(args).parseInto(::SamplerMainArgs).run {
                runSamplerSmtLib(
                    solverType = solverType,
                    smtLibFormulaFile = smtLibFormulaFile,
                    outCoverageFile = coverageFile,
                    coverageSamplerType = coverageSamplerType,
                    params
                )
            }
        }

        fun makeArgs(
            solverType: SolverType,
            inSmtLibFormula: File,
            outCoverageFile: File,
            coverageSamplerType: CoverageSamplerType,
            params: CoverageSamplerParams = CoverageSamplerParams.Empty
        ): Array<String> = listOfNotNull(
            "--${solverType.name}",
            "--in=${inSmtLibFormula.absolutePath}",
            "--out=${outCoverageFile.absolutePath}",
            "--${coverageSamplerType.name}",

            // params
            if (coverageSamplerType == CoverageSamplerType.GroupingModelsSampler && params.hasModelsGroupSizeParam()) "--mgs=${params.getModelsGroupSizeParam()}" else null,
            if (params.hasSolverTimeoutMillisParam()) "--stm=${params.getSolverTimeoutMillisParam()}" else null,
            if (params.hasCompleteModelsParam()) "--cm=${params.getCompleteModelsParam()}" else null
        ).toTypedArray()

        @JvmStatic
        fun runSamplerSmtLib(
            solverType: SolverType,
            smtLibFormulaFile: File,
            outCoverageFile: File,
            coverageSamplerType: CoverageSamplerType,
            samplerParams: CoverageSamplerParams = CoverageSamplerParams.Empty
        ) {
            if (outCoverageFile.exists())
                outCoverageFile.delete()
            else
                outCoverageFile.parentFile.mkdirs()

            KContext(simplificationMode = KContext.SimplificationMode.NO_SIMPLIFY).use { ctx ->

                val assertions = ctx.preprocessCoverageSamplerAssertions(smtLibFormulaFile)

                val predicates = BoolPredicatesExtractor(ctx).extractPredicates(assertions)

                val sampler = coverageSamplerType.makeCoverageSampler(
                    solverType,
                    ctx,
                    assertions,
                    ctx.mkBoolPredicatesUniverse(),
                    predicates,
                    samplerParams
                )

                try {
                    val coverage = sampler.use { it.computeCoverage() }
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
                } catch (e: UnsuitableFormulaCoverageSamplingException) {
                    PredicatesCoverageSamplingError(
                        PredicatesCoverageSamplingError.Reasons.InitiallyUnsuitableFormulaSatisfiability,
                        e.message.toString(),
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