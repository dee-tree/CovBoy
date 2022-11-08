package com.sokolov.covboy.coverage.provider

import com.microsoft.z3.coverage.unsatcore.UnsatCoreBasedCoverageSampler
import com.sokolov.covboy.coverage.CoverageSampler
import com.sokolov.covboy.prover.BaseProverEnvironment

class UnsatCoreBasedCoverageSamplerProvider : CoverageSamplerProvider() {
    override fun invoke(prover: BaseProverEnvironment): CoverageSampler = UnsatCoreBasedCoverageSampler(
        prover,
        prover.booleans
    )
}