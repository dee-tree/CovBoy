package com.sokolov.covboy.coverage

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals

class BoolCoverageSamplerTest : CoverageSamplerTestBase() {

    @TestFactory
    fun samplerTest(): Iterable<DynamicTest> = provideBoolSamplerTests().map { (dynTestData, sampler) ->
        DynamicTest.dynamicTest("$sampler - ${dynTestData.testName}") {
            val actualCoverage = sampler.computeCoverage()
            assertEquals(dynTestData.expectedCoverage, actualCoverage)
        }
    }

}