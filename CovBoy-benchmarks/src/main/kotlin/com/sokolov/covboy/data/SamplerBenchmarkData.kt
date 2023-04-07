package com.sokolov.covboy.data

import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class SamplerBenchmarkData(
    val inputFormulaPath: String,
    val duration: Duration,
    val checkSatCallsCount: Int,
    val checkSats: List<CheckSatData>,
    val predicatesCount: Int
) {

}