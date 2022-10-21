package com.sokolov.covboy.prover

import com.sokolov.covboy.assertNotContains
import com.sokolov.covboy.assertSat
import com.sokolov.covboy.assertUnsat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.sosy_lab.java_smt.SolverContextFactory
import org.sosy_lab.java_smt.SolverContextFactory.Solvers
import org.sosy_lab.java_smt.api.SolverContext
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ProverTest {
    private val solver: Solvers = Solvers.Z3
    private val context = SolverContextFactory.createSolverContext(solver)

    val prover = Prover(
        context.newProverEnvironment(
            SolverContext.ProverOptions.GENERATE_MODELS,
            SolverContext.ProverOptions.GENERATE_UNSAT_CORE
        ),
        context,
        emptyList()
    )

    @Test
    fun testSolve() {
        val a = prover.fm.booleanFormulaManager.makeVariable("a")
        val b = prover.fm.booleanFormulaManager.makeVariable("b")
        val ab = prover.fm.booleanFormulaManager.and(a, b)

        prover.addConstraint(ab)

        assertSat { prover.check() }

        assertEquals(true, prover.model.evaluate(a))
        assertEquals(true, prover.model.evaluate(b))

        val aNeg = prover.fm.booleanFormulaManager.not(a)
        prover.addConstraint(aNeg, true, "a_neg")

        assertUnsat { prover.check() }
        assertContains(prover.formulas, aNeg)
        assertContains(prover.formulas, ab)

        prover.disableConstraint(aNeg)
        assertSat { prover.check() }
        assertNotContains(prover.formulas, aNeg)
        assertContains(prover.formulas, ab)

        prover.enableConstraint(aNeg)
        assertUnsat { prover.check() }
        assertContains(prover.formulas, aNeg)
        assertContains(prover.formulas, ab)
    }

    @Test
    fun testUnsat1() {
        val f = prover.fm.booleanFormulaManager.makeFalse()
        prover.addConstraint(f)
        assertUnsat { prover.check() }
    }

    @Test
    fun testUnsat2() {
        val tr = prover.fm.booleanFormulaManager.makeTrue()
        val flse = prover.fm.booleanFormulaManager.not(tr)
        prover.addConstraint(flse)
        assertUnsat { prover.check() }
    }

    @AfterEach
    fun resetProverState() {
        prover.reset()
    }

}