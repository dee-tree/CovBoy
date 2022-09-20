package com.sokolov.z3cov

import com.microsoft.z3.Context
import com.microsoft.z3.Solver
import com.microsoft.z3.coverage.CoverageSampler
import com.microsoft.z3.coverage.unsatcore.UnsatCoreBasedCoverageSampler

class UnsatCoreBasedCoverageSamplerTest : CoverageSamplerTest() {
    override fun coverageSampler(solver: Solver, context: Context): CoverageSampler {
        return UnsatCoreBasedCoverageSampler(solver, context)
    }
}