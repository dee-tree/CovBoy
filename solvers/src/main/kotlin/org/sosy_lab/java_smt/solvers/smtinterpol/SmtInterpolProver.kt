package org.sosy_lab.java_smt.solvers.smtinterpol

import com.sokolov.covboy.solvers.formulas.Constraint
import com.sokolov.covboy.solvers.formulas.NonSwitchableConstraint
import com.sokolov.covboy.solvers.formulas.SwitchableConstraint
import com.sokolov.covboy.solvers.provers.ExtProverEnvironment
import com.sokolov.covboy.solvers.provers.Status
import de.uni_freiburg.informatik.ultimate.logic.Script
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.Formula
import org.sosy_lab.java_smt.api.Model
import org.sosy_lab.java_smt.api.ProverEnvironment
import org.sosy_lab.java_smt.basicimpl.withAssumptionsWrapper.basicProverWithAssumptionsDelegate
import java.io.File
import java.util.*

class SmtInterpolProver private constructor(
    internal val delegate: SmtInterpolTheoremProver
) : ProverEnvironment by delegate, ExtProverEnvironment {

    // ProverEnvironment

    override fun getUnsatCore(): List<BooleanFormula> {
        return delegate.env.unsatAssumptions.map { delegate.smtInterpolFormulaManager().encapsulateBooleanFormula(it) }
    }

    override fun unsatCoreOverAssumptions(assumptions: MutableCollection<BooleanFormula>): Optional<List<BooleanFormula>> {
        val unsatCore = unsatCore
        return if (unsatCore.isEmpty()) Optional.empty() else Optional.of(unsatCore)
    }


    override fun addConstraint(constraint: BooleanFormula): Void? {
        delegate.addConstraint(constraint)
        return null
    }


    // ExtProverEnvironment

    private var lastCheckAssumptions: List<BooleanFormula> = emptyList()

    override fun addConstraint(constraint: Constraint) {
        if (constraint is SwitchableConstraint) {
            addConstraint(constraint.asFormula)
        } else {
            check(constraint is NonSwitchableConstraint)
            // add in same way to track assertion in unsat cores
            addConstraint(delegate.mgr.booleanFormulaManager.implication(constraint.track, constraint.asFormula))
        }
    }

    override fun addConstraintsFromFile(smtFile: File): List<Constraint> {
        TODO("read from file")
    }

    override fun checkSat(assumptions: List<BooleanFormula>): Status {
        lastCheckAssumptions = assumptions.toList()

        return try {
            val status = delegate.env.checkSatAssuming(*assumptions.map { it.smtInterpolTerm() }.toTypedArray())
            when (status) {
                Script.LBool.SAT -> Status.SAT
                Script.LBool.UNSAT -> Status.UNSAT
                else -> Status.UNKNOWN
            }
        } catch (e: Exception) {
            Status.UNKNOWN
        }
    }

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

    override val modelAssignments: List<Model.ValueAssignment>
        get() = delegate.modelAssignments

    override val pushScopesSize: Int
        get() = size()

    override fun reset() {
        while (pushScopesSize > 0)
            pop()
    }

    override fun isSuitableFormula(formula: Formula): Boolean = formula.isSmtInterpolFormula()

    companion object {
        fun wrap(proverEnv: ProverEnvironment): SmtInterpolProver {
            return SmtInterpolProver(proverEnv.basicProverWithAssumptionsDelegate() as SmtInterpolTheoremProver)
        }
    }
}