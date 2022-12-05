package org.sosy_lab.java_smt.solvers.smtinterpol

import com.sokolov.covboy.solvers.formulas.Constraint
import com.sokolov.covboy.solvers.provers.ExtProverEnvironment
import com.sokolov.covboy.solvers.provers.Status
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
        return delegate.unsatCore
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
        if (lastCheckAssumptions.isNotEmpty()) {
            pop()
            lastCheckAssumptions = emptyList()
        }

        addConstraint(constraint.asFormula)
    }

    override fun addConstraintsFromFile(smtFile: File): List<Constraint> {
        TODO("read from file")
    }

    override fun checkSat(assumptions: List<BooleanFormula>): Status {

        if (lastCheckAssumptions.isNotEmpty()) {
            pop()
            lastCheckAssumptions = emptyList()

        }

        if (assumptions.isNotEmpty())
            push()

        lastCheckAssumptions = assumptions.toList()

        assumptions.forEach {
            addConstraint(it)
        }

        return try {
            val unsat = delegate.isUnsat
            if (unsat) Status.UNSAT else Status.SAT
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