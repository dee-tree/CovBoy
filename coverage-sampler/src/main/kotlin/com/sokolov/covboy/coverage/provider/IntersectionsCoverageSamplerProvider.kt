package com.sokolov.covboy.coverage.provider

import com.microsoft.z3.coverage.intersections.ModelsIntersectionCoverage
import com.sokolov.covboy.coverage.CoverageSampler
import com.sokolov.covboy.prover.BaseProverEnvironment

class IntersectionsCoverageSamplerProvider : CoverageSamplerProvider() {
    override fun invoke(prover: BaseProverEnvironment): CoverageSampler =
        ModelsIntersectionCoverage(prover, prover.booleans, 2)
}