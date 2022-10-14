package com.sokolov.covboy

import com.sokolov.covboy.coverage.CoverageSampler
import com.sokolov.covboy.prover.BaseProverEnvironment

abstract class CoverageSamplerProvider {
    abstract operator fun invoke(prover: BaseProverEnvironment): CoverageSampler
}