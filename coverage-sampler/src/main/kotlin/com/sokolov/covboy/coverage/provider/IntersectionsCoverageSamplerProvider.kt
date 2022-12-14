package com.sokolov.covboy.coverage.provider

import com.sokolov.covboy.coverage.sampler.CoverageSampler
import com.sokolov.covboy.coverage.sampler.impl.ModelsIntersectionCoverageSampler
import com.sokolov.covboy.solvers.provers.Prover

class IntersectionsCoverageSamplerProvider : CoverageSamplerProvider() {
    override fun invoke(prover: Prover): CoverageSampler = ModelsIntersectionCoverageSampler(
        prover,
        prover.booleans,
        2
    )
}