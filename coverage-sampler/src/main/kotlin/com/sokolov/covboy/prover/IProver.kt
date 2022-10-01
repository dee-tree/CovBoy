package com.sokolov.covboy.prover

import org.sosy_lab.java_smt.SolverContextFactory.Solvers
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.Formula
import org.sosy_lab.java_smt.api.ProverEnvironment
import org.sosy_lab.java_smt.api.SolverContext
import org.sosy_lab.java_smt.solvers.boolector.isBoolectorFormula
import org.sosy_lab.java_smt.solvers.cvc4.isCVC4Formula
import org.sosy_lab.java_smt.solvers.z3.isZ3Formula
import java.io.File

interface IProver : ProverEnvironment {

    val context: SolverContext
    val solver: Solvers
        get() = context.solverName

    fun isSolverSuitableFormula(formula: Formula): Boolean {
        return when (solver) {
            Solvers.Z3 -> formula.isZ3Formula()
            Solvers.CVC4 -> formula.isCVC4Formula()
            Solvers.BOOLECTOR -> formula.isBoolectorFormula()
            else -> error("Unsupported solver $solver")
        }
    }

    fun Formula.isSuitableForThisSolver(): Boolean = isSolverSuitableFormula(this)

    val unsatCoreWithAssumptions: List<BooleanFormula>

    val constraints: List<BooleanFormula>

    val booleans: Set<BooleanFormula>

    val checksStatistics: Map<String, ChecksCounter>

    val assertionsStorage: AssertionsStorage

    fun addConstraint(constraint: BooleanFormula, tag: String): Assertion
    fun getAssertionsByTag(tag: String): List<Assertion>
    fun getAssertionsByTag(onTag: (String) -> Boolean): List<Assertion>
    fun filterAssertions(filter: (Assertion) -> Boolean): List<Assertion>


    fun check(reason: String = ""): Status
    fun check(assumptions: List<BooleanFormula>, reason: String = ""): Status

    fun addConstraintsFromSmtLib(input: File): List<BooleanFormula>
}