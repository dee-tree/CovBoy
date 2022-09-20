package com.sokolov.smt.sampling

import com.microsoft.z3.Context
import com.microsoft.z3.Solver
import com.microsoft.z3.coverage.CoverageSampler
import com.microsoft.z3.coverage.intersections.ModelsIntersectionCoverage

class ModelsEnumerationOnIntersectionsTest : CoverageSamplerAgainstDullEnumerationTest() {
    override fun coverageSampler(solver: Solver, context: Context): CoverageSampler {
        return ModelsIntersectionCoverage(
            solver = solver,
            context = context,
            intersectionSize = 3,
            nonChangedCoverageIterationsLimit = 1
        )
    }
}