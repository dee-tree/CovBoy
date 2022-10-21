package com.sokolov.covboy.impl

import com.microsoft.z3.coverage.unsatcore.UnsatCoreBasedCoverageSampler
import com.sokolov.covboy.CoverageSamplerProvider
import com.sokolov.covboy.coverage.CoverageSampler
import com.sokolov.covboy.prover.BaseProverEnvironment

class UnsatCoreBasedCoverageSamplerProvider : CoverageSamplerProvider() {
    override fun invoke(prover: BaseProverEnvironment): CoverageSampler =
        UnsatCoreBasedCoverageSampler(prover, prover.booleans)
}