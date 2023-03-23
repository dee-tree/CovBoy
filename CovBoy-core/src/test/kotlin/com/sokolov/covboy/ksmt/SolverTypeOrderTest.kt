package com.sokolov.covboy.ksmt

import org.ksmt.runner.generated.models.SolverType
import kotlin.test.Test
import kotlin.test.assertEquals

class SolverTypeOrderTest {

    /**
     * SolverType ordinals are important due to enum serialization. see: [com.sokolov.covboy.PredicatesCoverageSerializer]
     */
    @Test
    fun testSolverTypeOrder() {
        assertEquals(0, SolverType.Z3.ordinal)
        assertEquals(1, SolverType.Bitwuzla.ordinal)
        assertEquals(2, SolverType.Yices.ordinal)
        assertEquals(3, SolverType.Cvc5.ordinal)
    }
}