package com.sokolov.covboy.coverage.provider

import com.sokolov.covboy.coverage.sampler.CoverageSampler
import com.sokolov.covboy.solvers.provers.Prover

abstract class CoverageSamplerProvider {
    abstract operator fun invoke(prover: Prover): CoverageSampler
}