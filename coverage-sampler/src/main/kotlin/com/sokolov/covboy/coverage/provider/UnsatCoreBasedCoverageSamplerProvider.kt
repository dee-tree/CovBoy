package com.sokolov.covboy.coverage.provider

import com.sokolov.covboy.coverage.sampler.CoverageSampler
import com.sokolov.covboy.solvers.provers.Prover

class UnsatCoreBasedCoverageSamplerProvider : CoverageSamplerProvider() {
    override fun invoke(prover: Prover): CoverageSampler = TODO()/*UnsatCoreBasedCoverageSampler(
        prover,
        prover.booleans
    )*/
}