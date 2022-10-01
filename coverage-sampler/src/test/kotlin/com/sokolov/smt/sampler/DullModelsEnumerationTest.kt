package com.sokolov.smt.sampler

import com.microsoft.z3.coverage.CoverageSampler
import com.microsoft.z3.coverage.ModelsEnumerationCoverage
import com.sokolov.smt.prover.IProver
import org.sosy_lab.java_smt.api.SolverContext

class DullModelsEnumerationTest : CoverageSamplerTest() {

    override fun coverageSampler(context: SolverContext, prover: IProver): CoverageSampler {
        return ModelsEnumerationCoverage(context, prover, prover.booleans)
    }
}

