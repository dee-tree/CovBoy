package org.sosy_lab.java_smt.solvers.boolector

import com.sokolov.covboy.solvers.formulas.Constraint
import com.sokolov.covboy.solvers.provers.ExtProverEnvironment
import com.sokolov.covboy.solvers.provers.Status
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.Formula
import org.sosy_lab.java_smt.api.Model
import org.sosy_lab.java_smt.api.ProverEnvironment
import java.io.File
import java.util.*

class BoolectorProver private constructor(
    internal val delegate: BoolectorTheoremProver
) : ProverEnvironment by delegate, ExtProverEnvironment {

    // ProverEnvironment

    /**
     * returns assumptions
     */
    override fun getUnsatCore(): List<BooleanFormula> {
        return boolectorUnsatCore()
    }

    override fun unsatCoreOverAssumptions(assumptions: MutableCollection<BooleanFormula>): Optional<List<BooleanFormula>> {
        val unsatCore = unsatCore
        return if (unsatCore.isEmpty()) Optional.empty() else Optional.of(unsatCore)
    }

    // ExtProverEnvironment

    override fun addConstraint(constraint: Constraint) {
        delegate.addConstraint(constraint.asFormula)
    }

    override fun addConstraintsFromFile(smtFile: File): List<Constraint> {
        TODO("Not yet implemented for boolector")
    }

    override fun isSuitableFormula(formula: Formula): Boolean = formula.isBoolectorFormula()

    override val pushScopesSize: Int
        get() = delegate.size()

    override val modelAssignments: List<Model.ValueAssignment>
        get() = delegate.modelAssignments

    override fun reset() {
        while (pushScopesSize > 0) {
            pop()
        }
    }

    override fun checkSat(assumptions: List<BooleanFormula>): Status = try {
        val unsat = delegate.isUnsatWithAssumptions(assumptions)
        if (unsat) Status.UNSAT else Status.SAT
    } catch (e: Exception) {
        Status.UNKNOWN
    }

    companion object {
        fun wrap(proverEnv: ProverEnvironment): BoolectorProver {
            return BoolectorProver(proverEnv as BoolectorTheoremProver)
        }
    }
}