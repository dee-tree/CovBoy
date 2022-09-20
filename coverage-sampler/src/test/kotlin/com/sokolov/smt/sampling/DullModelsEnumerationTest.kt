package com.sokolov.smt.sampling

import com.microsoft.z3.Context
import com.microsoft.z3.Solver
import com.microsoft.z3.coverage.CoverageSampler
import com.microsoft.z3.coverage.ModelsEnumerationCoverage

class DullModelsEnumerationTest : CoverageSamplerTest() {
    override fun coverageSampler(solver: Solver, context: Context): CoverageSampler {
        return ModelsEnumerationCoverage(solver, context)
    }
}

