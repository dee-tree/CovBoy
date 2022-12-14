package com.sokolov.covboy.coverage.provider

import com.sokolov.covboy.coverage.sampler.CoverageSampler
import com.sokolov.covboy.solvers.provers.Prover

class EnumerationCoverageSamplerProvider : CoverageSamplerProvider() {
    override fun invoke(prover: Prover): CoverageSampler = TODO() /*ModelsEnumerationCoverage(
        prover,
        prover.booleans
    )*/
}