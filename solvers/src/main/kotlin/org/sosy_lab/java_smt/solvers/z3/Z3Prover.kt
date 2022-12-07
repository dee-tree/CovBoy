package org.sosy_lab.java_smt.solvers.z3

import com.microsoft.z3.Native
import com.sokolov.covboy.solvers.formulas.Constraint
import com.sokolov.covboy.solvers.formulas.SwitchableConstraint
import com.sokolov.covboy.solvers.formulas.asNonSwitchableConstraint
import com.sokolov.covboy.solvers.provers.ExtProverEnvironment
import com.sokolov.covboy.solvers.provers.Status
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.Formula
import org.sosy_lab.java_smt.api.Model
import org.sosy_lab.java_smt.api.ProverEnvironment
import java.io.File
import java.util.*

class Z3Prover private constructor(
    internal val delegate: Z3TheoremProver
) : ProverEnvironment by delegate, ExtProverEnvironment {

    // ProverEnvironment

    /**
     * Returns assumptions
     */
    override fun getUnsatCore(): List<BooleanFormula> {
        // Z3 unsat core returns assumptions
        return z3UnsatCore()
    }

    override fun unsatCoreOverAssumptions(assumptions: MutableCollection<BooleanFormula>): Optional<List<BooleanFormula>> {
        val unsatCore = unsatCore
        return if (unsatCore.isEmpty()) Optional.empty() else Optional.of(unsatCore)
    }

    /**
     * Fix of Z3AbstractProver.addConstraint0, where created custom assumptions Z3_UNSAT_CORE_X,
     * and where **BUG** with double-ref decrease.
     */
    @Deprecated("Use addConstraint(Constraint) of ExtProverEnvironment")
    override fun addConstraint(constraint: BooleanFormula): Void? {
        assert(constraint)
        return null
    }


    // ExtProverEnvironment

    override fun addConstraint(constraint: Constraint) {
        if (constraint is SwitchableConstraint) {
            /*
            here might be:
            * assertAndTrack(constraint.asFormula, constraint.track)
            but assumptions installed and with "assert". Maybe javaSMT adds :named in assert?
             */
            assert(constraint.asFormula)
        } else {
            // but here we need track
            assertAndTrack(constraint.asFormula, constraint.track)
        }
    }

    override fun addConstraintsFromFile(smtFile: File): List<Constraint> {
        z3FromFile(smtFile)
        val formulas = z3Assertions()
        return formulas.map { (it as BooleanFormula).asNonSwitchableConstraint(delegate.z3FormulaManager()) }
    }

    override fun checkSat(assumptions: List<BooleanFormula>): Status = try {
        val unsat = if (assumptions.isEmpty()) delegate.isUnsat else delegate.isUnsatWithAssumptions(assumptions)
        if (unsat) Status.UNSAT else Status.SAT
    } catch (e: Exception) {
        Status.UNKNOWN
    }

    override val modelAssignments: List<Model.ValueAssignment>
        get() = delegate.modelAssignments

    override val pushScopesSize: Int
        get() = size()

    override fun reset() {
        Native.solverReset(z3Context(), z3Solver())
    }

    override fun isSuitableFormula(formula: Formula): Boolean = formula.isZ3Formula()

    override fun push() {
        delegate.push()
    }

    override fun pop() {
        delegate.pop()
    }

    override fun close() {
        reset()
        delegate.close()
    }

    fun assertAndTrack(constraint: BooleanFormula, assumption: Formula) {
        Native.solverAssertAndTrack(z3Context(), z3Solver(), constraint.z3Expr, assumption.z3Expr)
    }

    fun assert(constraint: BooleanFormula) {
        delegate.assertContraint(constraint.z3Expr)
    }

    companion object {
        fun wrap(proverEnv: ProverEnvironment): Z3Prover {
            return Z3Prover(proverEnv as Z3TheoremProver)
        }
    }
}
