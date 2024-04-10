package com.sokolov.covboy.statistics

import com.sokolov.covboy.sampler.params.CoverageSamplerParams
import com.sokolov.covboy.sampler.params.CoverageSamplerParamsBuilder
import org.ksmt.sort.KSort
import java.io.File

interface CoverageSamplerExt<S : KSort> {
    val coveredSatValuesCount: Int
    val statistics: SamplerStatistics

    object ParamKeys {
        const val Statistics = "Statistics"
        const val StatisticsFile = "StatisticsFile"
    }
}


fun CoverageSamplerParams.hasStatisticsFileParam(): Boolean =
    hasStringParam(CoverageSamplerExt.ParamKeys.StatisticsFile)

fun CoverageSamplerParams.getStatisticsFileParam(): String = getString(CoverageSamplerExt.ParamKeys.StatisticsFile)
fun CoverageSamplerParams.getStatisticsFileParamAsFile(): File = File(getStatisticsFileParam())

fun CoverageSamplerParamsBuilder.putStatisticsFile(value: String) {
    putParam(CoverageSamplerExt.ParamKeys.StatisticsFile, value)
}
