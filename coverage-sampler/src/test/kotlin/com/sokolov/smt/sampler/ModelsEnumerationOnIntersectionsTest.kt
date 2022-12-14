package com.sokolov.smt.sampler

import com.sokolov.covboy.coverage.sampler.CoverageSampler
import com.sokolov.covboy.coverage.sampler.impl.ModelsIntersectionCoverageSampler
import com.sokolov.covboy.solvers.provers.Prover
import org.sosy_lab.java_smt.SolverContextFactory


class ModelsEnumerationOnIntersectionsTest : CoverageSamplerTest() {

    override fun coverageSampler(prover: Prover): CoverageSampler {
        return ModelsIntersectionCoverageSampler(
            prover,
            prover.booleans,
            2
        )
    }

    override fun provideSolver(): SolverContextFactory.Solvers {
        return SolverContextFactory.Solvers.BOOLECTOR
    }
}