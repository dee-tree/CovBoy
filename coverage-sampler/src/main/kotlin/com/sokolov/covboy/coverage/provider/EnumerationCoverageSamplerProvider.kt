package com.sokolov.covboy.coverage.provider

import com.microsoft.z3.coverage.ModelsEnumerationCoverage
import com.sokolov.covboy.coverage.CoverageSampler
import com.sokolov.covboy.prover.BaseProverEnvironment

class EnumerationCoverageSamplerProvider : CoverageSamplerProvider() {
    override fun invoke(prover: BaseProverEnvironment): CoverageSampler = ModelsEnumerationCoverage(
        prover,
        prover.booleans
    )
}