package com.sokolov.covboy.sampler.main

import com.sokolov.covboy.sampler.CoverageSampler
import com.sokolov.covboy.sampler.CoverageSamplerType
import com.sokolov.covboy.sampler.impl.GroupingModelsCoverageSampler
import com.sokolov.covboy.sampler.impl.putModelsGroupSizeParam
import com.sokolov.covboy.sampler.params.CoverageSamplerParams
import com.sokolov.covboy.sampler.putCompleteModels
import com.sokolov.covboy.sampler.putSolverTimeoutMillis
import com.sokolov.covboy.statistics.putStatistics
import com.sokolov.covboy.statistics.putStatisticsFile
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import java.io.File

open class SamplerParamArgs(parser: ArgParser) {

    val coverageSamplerType by parser.mapping(
        "--${CoverageSamplerType.PredicatesPropagatingSampler}" to CoverageSamplerType.PredicatesPropagatingSampler,
        "--baseline" to CoverageSamplerType.BaselinePredicatePropagatingSampler,
        "--${CoverageSamplerType.GroupingModelsSampler}" to CoverageSamplerType.GroupingModelsSampler,
        help = "Name of the coverage sampler type. Possible variants: ${
            CoverageSamplerType.values().joinToString(" / ")
        }"
    ).default { CoverageSamplerType.PredicatesPropagatingSampler }

    val solverTimeoutMillis by parser.storing(
        "--stm", "--solverTimeoutMillis",
        help = "Timeout, in millis, on each solver check-sat() operation; 0 to disable timeout"
    ) { this.toLong() }
        .default { CoverageSampler.DEFAULT_SOLVER_TIMEOUT.inWholeMilliseconds }
        .addValidator { if (value < 0) throw IllegalArgumentException("Solver timeout millis must be non-negative!") }

    val completeModels by parser.storing(
        "--completeModels", "--cm",
        help = "Should force solver to provide complete models (with assigning value for each predicate) - true; Or to provide incomplete models (to cover non-model-core predicates lazily) - false"
    ) { this.toBooleanStrict() }.default { CoverageSampler.DEFAULT_COMPLETE_MODELS }

    val modelsGroupSize by parser.storing(
        "--mgs", "--modelsGroupSize",
        help = "Size of bounded models in group. Valuable in ${CoverageSamplerType.GroupingModelsSampler}"
    ) { this.toInt() }
        .default { GroupingModelsCoverageSampler.DEFAULT_MODELS_GROUP_SIZE }
        .addValidator { if (value < 1) throw IllegalArgumentException("Models groupd size must be positive value!") }

    val statistics: Boolean by parser.flagging(
        "--statistics", "--stat",
        help = "Collect statistics on coverage sampler"
    ).default { false }

    val statisticsFile: File by parser.storing(
        "--sf", "--statfile",
        help = "Path to output of statistics if enabled (.csv)"
    ) { File(this) }

    open val params: CoverageSamplerParams
        get() = CoverageSamplerParams.build {
            putSolverTimeoutMillis(solverTimeoutMillis)
            putCompleteModels(completeModels)

            if (coverageSamplerType == CoverageSamplerType.GroupingModelsSampler) {
                putModelsGroupSizeParam(modelsGroupSize)
            }

            if (statistics) {
                putStatistics(true)
                putStatisticsFile(statisticsFile.absolutePath)
            }
        }


}