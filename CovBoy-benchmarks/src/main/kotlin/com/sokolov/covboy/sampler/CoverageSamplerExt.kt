package com.sokolov.covboy.sampler

import org.ksmt.sort.KSort

interface CoverageSamplerExt<S : KSort> {
    val coveredValuesCount: Int
}