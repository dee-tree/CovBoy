package com.sokolov.covboy.statistics

import com.sokolov.covboy.sampler.params.CoverageSamplerParams
import com.sokolov.covboy.sampler.params.CoverageSamplerParamsBuilder
import org.ksmt.sort.KSort

interface CoverageSamplerExt<S : KSort> {
    val coveredSatValuesCount: Int
    val statistics: SamplerStatistics

    object ParamKeys {
        const val Statistics = "Statistics"
        const val StatisticsFile = "StatisticsFile"
    }
}


fun CoverageSamplerParams.hasStatisticsParam(): Boolean = hasBoolParam(CoverageSamplerExt.ParamKeys.Statistics)
fun CoverageSamplerParams.hasStatisticsFileParam(): Boolean =
    hasStringParam(CoverageSamplerExt.ParamKeys.StatisticsFile)

fun CoverageSamplerParams.getStatisticsParam(): Boolean = getBool(CoverageSamplerExt.ParamKeys.Statistics)
fun CoverageSamplerParams.getStatisticsFileParam(): String = getString(CoverageSamplerExt.ParamKeys.StatisticsFile)

fun CoverageSamplerParamsBuilder.putStatistics(value: Boolean) {
    putParam(CoverageSamplerExt.ParamKeys.Statistics, value)
}

fun CoverageSamplerParamsBuilder.putStatisticsFile(value: String) {
    putParam(CoverageSamplerExt.ParamKeys.StatisticsFile, value)
}
