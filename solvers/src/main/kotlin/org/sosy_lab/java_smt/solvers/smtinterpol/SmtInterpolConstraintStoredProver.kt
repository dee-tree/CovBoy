package org.sosy_lab.java_smt.solvers.smtinterpol

import com.sokolov.covboy.solvers.provers.secondary.AbstractConstraintStoredProver
import org.sosy_lab.java_smt.api.BooleanFormula

class SmtInterpolConstraintStoredProver internal constructor(
    private val smtInterpolProver: SmtInterpolProver
) : AbstractConstraintStoredProver(smtInterpolProver) {
    override fun getUnsatCore(): List<BooleanFormula> {
        return super.getUnsatCore()
    }
}