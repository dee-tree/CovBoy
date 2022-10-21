package com.sokolov.covboy

import com.sokolov.covboy.impl.UnsatCoreBasedCoverageSamplerProvider
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.sosy_lab.java_smt.SolverContextFactory.Solvers
import java.io.File

class UnsatCoreBasedCoverageEstimatorTest : CoverageEstimatorTest() {

    @ParameterizedTest
    @MethodSource("provideCoverageEstimatorInput")
    override fun test(origin: Solvers, other: Solvers, input: File) {
        test(UnsatCoreBasedCoverageSamplerProvider(), origin, other, input)
    }
}