package org.sosy_lab.java_smt.solvers.princess

import ap.parser.IExpression
import org.sosy_lab.java_smt.api.Formula
import org.sosy_lab.java_smt.basicimpl.formulaInfo
import org.sosy_lab.java_smt.basicimpl.isAbstractFormula


fun Formula.isPrincessFormula(): Boolean {
    if (!this.isAbstractFormula()) return false

    val formulaInfo = this.formulaInfo()
    return formulaInfo is IExpression
}