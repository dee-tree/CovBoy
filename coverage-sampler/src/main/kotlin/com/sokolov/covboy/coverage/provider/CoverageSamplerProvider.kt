package com.sokolov.covboy.coverage.provider

import com.sokolov.covboy.coverage.CoverageSampler
import com.sokolov.covboy.prover.BaseProverEnvironment

abstract class CoverageSamplerProvider {
    abstract operator fun invoke(prover: BaseProverEnvironment): CoverageSampler
}