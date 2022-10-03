package com.sokolov.covboy.impl

import com.microsoft.z3.coverage.ModelsEnumerationCoverage
import com.sokolov.covboy.CoverageSamplerProvider
import com.sokolov.covboy.coverage.CoverageSampler
import com.sokolov.covboy.prover.IProver

class ModelEnumerationCoverageSamplerProvider : CoverageSamplerProvider() {
    override fun invoke(prover: IProver): CoverageSampler = ModelsEnumerationCoverage(prover, prover.booleans)
}