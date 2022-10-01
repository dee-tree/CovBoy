package com.sokolov.smt.prover

import com.sokolov.smt.Status
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

    val constraints: List<BooleanFormula>
    val booleans: Set<BooleanFormula>

    fun check(): Status
    fun check(assumptions: List<BooleanFormula>): Status

    fun addConstraintsFromSmtLib(input: File): List<BooleanFormula>
}