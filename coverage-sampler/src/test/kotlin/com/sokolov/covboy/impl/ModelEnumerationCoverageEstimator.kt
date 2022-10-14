package com.sokolov.covboy.impl

import com.microsoft.z3.coverage.ModelsEnumerationCoverage
import com.sokolov.covboy.CoverageSamplerProvider
import com.sokolov.covboy.coverage.CoverageSampler
import com.sokolov.covboy.prover.BaseProverEnvironment

class ModelEnumerationCoverageSamplerProvider : CoverageSamplerProvider() {
    override fun invoke(prover: BaseProverEnvironment): CoverageSampler =
        ModelsEnumerationCoverage(prover, prover.booleans)
}