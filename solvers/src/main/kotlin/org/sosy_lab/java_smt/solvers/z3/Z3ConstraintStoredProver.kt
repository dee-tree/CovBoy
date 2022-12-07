package org.sosy_lab.java_smt.solvers.z3

import com.sokolov.covboy.solvers.formulas.SwitchableConstraint
import com.sokolov.covboy.solvers.provers.secondary.AbstractConstraintStoredProver
import org.sosy_lab.java_smt.api.BooleanFormula

class Z3ConstraintStoredProver internal constructor(private val z3Prover: Z3Prover): AbstractConstraintStoredProver(z3Prover)  {

    /**
     * @return track of Constraint
     */
    override fun getUnsatCore(): List<BooleanFormula> {
//        val enabledConstraints = constraints.filter { it.enabled }
        return super.getUnsatCore()
//        return super.getUnsatCore().map { ucTrack ->
//            enabledConstraints.first { it.track == ucTrack }
//            val constraint = enabledConstraints.first { it.track == ucTrack }
//            if (constraint is SwitchableConstraint) constraint.original else constraint.asFormula
//        }
    }
}