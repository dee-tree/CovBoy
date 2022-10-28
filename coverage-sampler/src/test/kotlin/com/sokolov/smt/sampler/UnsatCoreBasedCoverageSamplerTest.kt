package com.sokolov.smt.sampler

import com.microsoft.z3.coverage.unsatcore.UnsatCoreBasedCoverageSampler
import com.sokolov.covboy.coverage.CoverageSampler
import com.sokolov.covboy.prover.BaseProverEnvironment

class UnsatCoreBasedCoverageSamplerTest : CoverageSamplerTest() {
    override fun coverageSampler(prover: BaseProverEnvironment): CoverageSampler {
        return UnsatCoreBasedCoverageSampler(prover, prover.booleans)
    }
}
