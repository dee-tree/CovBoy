package org.sosy_lab.java_smt.solvers.z3

import com.sokolov.covboy.solvers.provers.secondary.AbstractConstraintStoredProver
import org.sosy_lab.java_smt.api.BooleanFormula

class Z3ConstraintStoredProver internal constructor(private val z3Prover: Z3Prover): AbstractConstraintStoredProver(z3Prover)  {
    override fun getUnsatCore(): List<BooleanFormula> {
        val enabledConstraints = switchableConstraints.filter { it.enabled }
        return super.getUnsatCore().map { ucAssumption -> enabledConstraints.first { it.assumption == ucAssumption }.original }
    }
}