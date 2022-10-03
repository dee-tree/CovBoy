package com.sokolov.covboy

import com.sokolov.covboy.coverage.CoverageSampler
import com.sokolov.covboy.prover.IProver

abstract class CoverageSamplerProvider {
    abstract operator fun invoke(prover: IProver): CoverageSampler
}