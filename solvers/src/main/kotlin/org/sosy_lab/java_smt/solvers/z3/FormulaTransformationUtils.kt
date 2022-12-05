package org.sosy_lab.java_smt.solvers.z3

import org.sosy_lab.java_smt.api.Formula
import org.sosy_lab.java_smt.api.FormulaManager
import org.sosy_lab.java_smt.api.SolverContext

internal fun Z3Formula.transform(
    context: Z3SolverContext,
    newFormulaManager: FormulaManager
): Formula {
    return Z3FormulaTransformer(context, this, newFormulaManager).transform()
}

fun <T : Formula> T.z3FormulaTransform(
    context: SolverContext,
    newFormulaManager: FormulaManager
): T {
    return (this as Z3Formula).transform(context as Z3SolverContext, newFormulaManager) as T
}