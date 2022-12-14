package com.sokolov.covboy.coverage

import com.sokolov.covboy.assertAll
import com.sokolov.covboy.assertNotEmpty
import com.sokolov.covboy.coverage.provider.CoverageSamplerProvider
import com.sokolov.covboy.coverage.provider.IntersectionsCoverageSamplerProvider
import com.sokolov.covboy.solvers.formulas.asNonSwitchableConstraint
import com.sokolov.covboy.solvers.provers.Prover
import com.sokolov.covboy.solvers.provers.provider.makeProver
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.sosy_lab.java_smt.SolverContextFactory
import org.sosy_lab.java_smt.api.BooleanFormula
import java.util.stream.Stream
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SamplerTest {
    @ParameterizedTest
    @MethodSource("provideProverParameters")
    fun simpleCoverageTest(prover: Prover, provideSampler: CoverageSamplerProvider) {

        val a = prover.fm.booleanFormulaManager.makeVariable("a")
        val aConstr = a.asNonSwitchableConstraint(prover.fm)
        prover.addConstraint(aConstr)

        val sampler = provideSampler(prover)
        val coverage = sampler.computeCoverage()

        assertEquals(0.5, coverage.coverageNumber)
        assertFalse(coverage.isEmpty())
        assertNotEmpty(coverage.atomsCoverage)
        assertEquals(1, coverage.atomsCoverage.size) // true value on "a"
        assertAll(coverage.atomsCoverage.map { it.expr }, { f: BooleanFormula -> f == a })
        assertEquals(1, coverage.atomsCoverage.first().values.size)
        assertContains(coverage.atomsCoverage.first().values, prover.fm.booleanFormulaManager.makeBoolean(true))

        prover.close()
    }

    @ParameterizedTest
    @MethodSource("provideProverParameters")
    fun simpleCoverageTest2(prover: Prover, provideSampler: CoverageSamplerProvider) {

        val a = prover.fm.booleanFormulaManager.makeVariable("a")
        val expr = prover.fm.booleanFormulaManager.or(a, prover.fm.booleanFormulaManager.not(a))
        val aConstr = a.asNonSwitchableConstraint(prover.fm)
        prover.addConstraint(expr.asNonSwitchableConstraint(prover.fm))

        val sampler = provideSampler(prover)
        val coverage = sampler.computeCoverage()

        assertNotEmpty(coverage.atomsCoverage)
        assertEquals(1.0, coverage.coverageNumber)
        assertFalse(coverage.isEmpty())
        assertEquals(2, coverage.atomsCoverage.size) // true & false values on "a"
        assertAll(coverage.atomsCoverage.map { it.expr }, { f: BooleanFormula -> f == a })

        prover.close()
    }

    companion object {
        @JvmStatic
        fun provideProverParameters(): Stream<Arguments> = Stream.of(*(
            listOf(makeProver(true, SolverContextFactory.Solvers.Z3)) + (SolverContextFactory.Solvers.values()
            .toList() - SolverContextFactory.Solvers.MATHSAT5 - SolverContextFactory.Solvers.CVC4 - SolverContextFactory.Solvers.PRINCESS - SolverContextFactory.Solvers.YICES2 - SolverContextFactory.Solvers.Z3).map {
            makeProver(false, it)
        }).map {
            Arguments.of(
                it,
                IntersectionsCoverageSamplerProvider()
            )
        }.toTypedArray()
        )
    }
}