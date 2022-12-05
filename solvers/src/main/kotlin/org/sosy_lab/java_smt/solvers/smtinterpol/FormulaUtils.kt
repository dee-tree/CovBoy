package org.sosy_lab.java_smt.solvers.smtinterpol

import de.uni_freiburg.informatik.ultimate.logic.Term
import org.sosy_lab.java_smt.api.Formula
import org.sosy_lab.java_smt.basicimpl.formulaInfo
import org.sosy_lab.java_smt.basicimpl.isAbstractFormula

fun Formula.isSmtInterpolFormula(): Boolean {
    if (!this.isAbstractFormula()) return false

    val formulaInfo = this.formulaInfo()
    return formulaInfo is Term
}
