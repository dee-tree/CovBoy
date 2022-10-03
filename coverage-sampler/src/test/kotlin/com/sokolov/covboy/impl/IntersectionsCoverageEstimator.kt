package com.sokolov.covboy.impl

import com.microsoft.z3.coverage.intersections.ModelsIntersectionCoverage
import com.sokolov.covboy.CoverageSamplerProvider
import com.sokolov.covboy.coverage.CoverageSampler
import com.sokolov.covboy.prover.IProver

class IntersectionsCoverageSamplerProvider : CoverageSamplerProvider() {
    override fun invoke(prover: IProver): CoverageSampler = ModelsIntersectionCoverage(prover, prover.booleans)
}