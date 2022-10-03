package com.sokolov.smt.sampler

import com.microsoft.z3.coverage.ModelsEnumerationCoverage
import com.sokolov.covboy.coverage.CoverageSampler
import com.sokolov.covboy.prover.IProver

class DullModelsEnumerationTest : CoverageSamplerTest() {

    override fun coverageSampler(prover: IProver): CoverageSampler {
        return ModelsEnumerationCoverage(prover, prover.booleans)
    }
}

