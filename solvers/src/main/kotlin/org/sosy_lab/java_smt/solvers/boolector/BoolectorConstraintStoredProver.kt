package org.sosy_lab.java_smt.solvers.boolector

import com.sokolov.covboy.solvers.provers.secondary.AbstractConstraintStoredProver
import org.sosy_lab.java_smt.api.BooleanFormula

class BoolectorConstraintStoredProver internal constructor(
    private val boolectorProver: BoolectorProver
) : AbstractConstraintStoredProver(boolectorProver) {
    override fun getUnsatCore(): List<BooleanFormula> {
        val enabledConstraints = switchableConstraints.filter { it.enabled }
        return super.getUnsatCore().map { ucAssumption -> enabledConstraints.first { it.assumption == ucAssumption }.original }
    }
}