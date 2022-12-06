package com.sokolov.covboy.solvers

import com.sokolov.covboy.solvers.formulas.asSwitchableConstraint
import com.sokolov.covboy.solvers.provers.Prover
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AssumptionsProverTest : ProverTest() {
    @ParameterizedTest
    @MethodSource("provideProverParameters")
    fun testAssumptions1(prover: Prover) {
        val fls = prover.fm.booleanFormulaManager.makeBoolean(false).asSwitchableConstraint(enabled = false, fm = prover.fm)
        assertFalse { fls.enabled }

        prover.addConstraint(fls)
        assertSat { prover.checkSat() }
        assertUnsat { prover.checkSat(fls.assumption) }

        prover.enableConstraint(fls)
        assertTrue { fls.enabled }

        assertUnsat { prover.checkSat() }
    }
}