package com.sokolov.covboy

import com.sokolov.covboy.impl.IntersectionsCoverageSamplerProvider
import com.sokolov.covboy.impl.ModelEnumerationCoverageSamplerProvider
import com.sokolov.covboy.prover.IProver
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class IntersectionsCoverageEstimatorTest : CoverageEstimatorTest() {

    @ParameterizedTest
    @MethodSource("provideCoverageEstimatorInput")
    fun test(baseProver: IProver, otherProver: IProver) {
        test(IntersectionsCoverageSamplerProvider(), baseProver, otherProver)
    }
}