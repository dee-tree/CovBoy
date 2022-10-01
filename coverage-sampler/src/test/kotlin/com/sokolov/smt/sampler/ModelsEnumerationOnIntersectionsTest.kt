package com.sokolov.smt.sampler

import com.microsoft.z3.coverage.intersections.ModelsIntersectionCoverage
import com.sokolov.covboy.coverage.CoverageSampler
import com.sokolov.covboy.prover.IProver
import org.sosy_lab.java_smt.api.SolverContext

// TODO: ModelsEnumerationOnIntersectionsTest
/*class ModelsEnumerationOnIntersectionsTest : CoverageSamplerAgainstDullEnumerationTest() {

    override fun coverageSampler(context: SolverContext, formulas: List<BooleanFormula>): CoverageSampler {
        return ModelsIntersectionCoverage(
            context = context,
            formulas = formulas,
            intersectionSize = 3,
            nonChangedCoverageIterationsLimit = 1
        )
    }
}*/

class ModelsEnumerationOnIntersectionsTest : CoverageSamplerTest() {

    override fun coverageSampler(context: SolverContext, prover: IProver): CoverageSampler {
        return ModelsIntersectionCoverage(
            context,
            prover,
            prover.booleans,
            2
        )
    }
}