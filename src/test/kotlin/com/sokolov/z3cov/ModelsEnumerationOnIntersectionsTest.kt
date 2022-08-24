package com.sokolov.z3cov

import com.microsoft.z3.Context
import com.microsoft.z3.Solver
import com.microsoft.z3.coverage.CoverageSampler
import com.microsoft.z3.coverage.ModelsIntersectionCoverage

class ModelsEnumerationOnIntersectionsTest : CoverageSamplerAgainstDullEnumerationTest() {
    override fun testCoverageSampler(solver: Solver, context: Context): CoverageSampler {
        return ModelsIntersectionCoverage(solver = solver, context = context)
    }
}

