package com.sokolov.covboy.solvers

import com.sokolov.covboy.solvers.formulas.asNonSwitchableConstraint
import com.sokolov.covboy.solvers.formulas.asSwitchableConstraint
import com.sokolov.covboy.solvers.provers.Prover
import com.sokolov.covboy.solvers.provers.secondary.SecondaryProver
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.sosy_lab.java_smt.SolverContextFactory
import org.sosy_lab.java_smt.SolverContextFactory.Solvers
import org.sosy_lab.java_smt.api.ProverEnvironment
import org.sosy_lab.java_smt.api.SolverContext
import java.util.stream.Stream
import kotlin.test.assertContains
import kotlin.test.assertEquals

class ProverTest {
    @ParameterizedTest
    @MethodSource("provideProverParameters")
    fun testSolve(prover: Prover) {
        val a = prover.fm.booleanFormulaManager.makeVariable("a")
        val b = prover.fm.booleanFormulaManager.makeVariable("b")
        val ab = prover.fm.booleanFormulaManager.and(a, b).asNonSwitchableConstraint()

        prover.addConstraint(ab)

        assertSat { prover.checkSat() }

        assertEquals(true, prover.getModel().evaluate(a))
        assertEquals(true, prover.getModel().evaluate(b))

        val aNeg = prover.fm.booleanFormulaManager.not(a).asSwitchableConstraint("a_neg", true, prover.fm)
        prover.addConstraint(aNeg)

        assertUnsat { prover.checkSat() }
        assertContains(prover.formulas, aNeg.original)
        assertContains(prover.assumedFormulas, aNeg.asFormula)
        assertContains(prover.formulas, ab.asFormula)

        prover.disableConstraint(aNeg)
        assertSat { prover.checkSat() }
        assertNotContains(prover.formulas, aNeg.original)
        assertNotContains(prover.assumptions, aNeg.assumption)
        assertContains(prover.formulas, ab.asFormula)

        prover.enableConstraint(aNeg)
        assertUnsat { prover.checkSat() }
        assertContains(prover.formulas, aNeg.original)
        assertContains(prover.assumedFormulas, aNeg.asFormula)
        assertContains(prover.assumptions, aNeg.assumption)
        assertContains(prover.formulas, ab.asFormula)

        prover.close()
    }

    @ParameterizedTest
    @MethodSource("provideProverParameters")
    fun testSolve2(prover: Prover) {
        val a = prover.fm.booleanFormulaManager.makeVariable("a")
        val b = prover.fm.booleanFormulaManager.makeVariable("b")
        val c = prover.fm.booleanFormulaManager.makeVariable("d")

        val orabc = prover.fm.booleanFormulaManager.or(a, b, c).asNonSwitchableConstraint()
        prover.addConstraint(orabc)
        assertSat { prover.checkSat() }
        assertContains(prover.formulas, orabc.asFormula)

        val notabc = prover.fm.booleanFormulaManager.not(prover.fm.booleanFormulaManager.or(a, b, c)).asSwitchableConstraint(fm = prover.fm)
        prover.addConstraint(notabc)

        assertUnsat { prover.checkSat() }
        assertContains(prover.formulas, orabc.asFormula)
        assertContains(prover.assumedFormulas, notabc.asFormula)
        assertContains(prover.assumptions, notabc.assumption)

        prover.disableConstraint(notabc)

        assertSat { prover.checkSat() }
        assertContains(prover.formulas, orabc.asFormula)
        assertNotContains(prover.formulas, notabc.original)

        val aConstraint = a.asSwitchableConstraint(fm = prover.fm)

        prover.addConstraint(aConstraint)
        assertSat { prover.checkSat() }
        assertContains(prover.formulas, orabc.asFormula)
        assertNotContains(prover.formulas, notabc.original)
        assertContains(prover.formulas, a)

        prover.enableConstraint(notabc)
        assertUnsat { prover.checkSat() }
        assertContains(prover.formulas, orabc.asFormula)
        assertContains(prover.formulas, notabc.original)
        assertContains(prover.formulas, a)

        prover.close()
    }

    @ParameterizedTest
    @MethodSource("provideProverParameters")
    fun testUnsat1(prover: Prover) {
        val f = prover.fm.booleanFormulaManager.makeFalse().asNonSwitchableConstraint()
        prover.addConstraint(f)
        assertUnsat { prover.checkSat() }

        prover.close()
    }

    @ParameterizedTest
    @MethodSource("provideProverParameters")
    fun testUnsat2(prover: Prover) {
        val tr = prover.fm.booleanFormulaManager.makeTrue()
        val flse = prover.fm.booleanFormulaManager.not(tr).asNonSwitchableConstraint()
        prover.addConstraint(flse)
        assertUnsat { prover.checkSat() }

        prover.close()
    }

    @ParameterizedTest
    @MethodSource("provideProverParameters")
    fun testUnsatCore(prover: Prover) {
        assumeTrue(prover.solverName != Solvers.MATHSAT5)
        assumeTrue(prover.solverName != Solvers.YICES2)

        val a = prover.fm.booleanFormulaManager.makeVariable("a")
        val b = prover.fm.booleanFormulaManager.makeVariable("b")
        val c = prover.fm.booleanFormulaManager.makeVariable("d")

        val orabc = prover.fm.booleanFormulaManager.or(a, b, c).asNonSwitchableConstraint()
        prover.addConstraint(orabc)
        assertSat { prover.checkSat() }
        assertContains(prover.formulas, orabc.asFormula)

        val notabc = prover.fm.booleanFormulaManager.not(prover.fm.booleanFormulaManager.or(a, b, c)).asSwitchableConstraint(fm = prover.fm)
        prover.addConstraint(notabc)

        assertUnsat { prover.checkSat() }

        assertContains(prover.getUnsatCore(), notabc.original)

        prover.disableConstraint(notabc)

        assertSat { prover.checkSat() }
        assertContains(prover.formulas, orabc.asFormula)
        assertNotContains(prover.formulas, notabc.original)

        val aConstraint = a.asSwitchableConstraint(fm = prover.fm)

        prover.addConstraint(aConstraint)
        assertSat { prover.checkSat() }
        assertContains(prover.formulas, orabc.asFormula)
        assertNotContains(prover.formulas, notabc.original)
        assertContains(prover.formulas, a)

        prover.enableConstraint(notabc)
        assertUnsat { prover.checkSat() }
        assertContains(prover.formulas, orabc.asFormula)
        assertContains(prover.formulas, notabc.original)
        assertContains(prover.formulas, a)

        prover.close()
    }

    companion object {
        fun makeContext(solver: Solvers): SolverContext = SolverContextFactory.createSolverContext(solver)

        fun makeProverEnvironment(context: SolverContext): ProverEnvironment = context.newProverEnvironment(
            SolverContext.ProverOptions.GENERATE_MODELS,
            SolverContext.ProverOptions.GENERATE_UNSAT_CORE
        )

        fun makeProver(primary: Boolean, solver: Solvers): Prover {
            val ctx = makeContext(solver)
            val proverEnv = makeProverEnvironment(ctx)

            return if (primary) {
                Prover(proverEnv, ctx)
            } else {
                val primaryProver = makeProver(true, Solvers.Z3)
                SecondaryProver(proverEnv, ctx, primaryProver)
            }
        }

        @JvmStatic
        fun provideProverParameters(): Stream<Arguments> = Stream.of(*((Solvers.values().toList() - Solvers.MATHSAT5 - Solvers.CVC4 - Solvers.PRINCESS - Solvers.YICES2).map {
            makeProver(true, it)
        } + (Solvers.values().toList() - Solvers.MATHSAT5 - Solvers.CVC4 - Solvers.PRINCESS - Solvers.YICES2).map {
            makeProver(false, it)
        }).map {
            Arguments.of(it)
        }.toTypedArray()
        )
    }

}