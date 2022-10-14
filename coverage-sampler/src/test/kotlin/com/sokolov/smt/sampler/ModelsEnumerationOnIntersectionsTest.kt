package com.sokolov.smt.sampler

import com.microsoft.z3.coverage.intersections.ModelsIntersectionCoverage
import com.sokolov.covboy.coverage.CoverageSampler
import com.sokolov.covboy.prover.BaseProverEnvironment


class ModelsEnumerationOnIntersectionsTest : CoverageSamplerTest() {

    override fun coverageSampler(prover: BaseProverEnvironment): CoverageSampler {
        return ModelsIntersectionCoverage(
            prover,
            prover.booleans,
            2
        )
    }
}