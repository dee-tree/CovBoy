package com.sokolov.smt.sampler

import com.microsoft.z3.coverage.ModelsEnumerationCoverage
import com.sokolov.covboy.coverage.CoverageSampler
import com.sokolov.covboy.prover.BaseProverEnvironment

class DullModelsEnumerationTest : CoverageSamplerTest() {

    override fun coverageSampler(prover: BaseProverEnvironment): CoverageSampler {
        return ModelsEnumerationCoverage(prover, prover.booleans)
    }
}

