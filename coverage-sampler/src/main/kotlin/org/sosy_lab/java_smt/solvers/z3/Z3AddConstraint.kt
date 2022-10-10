package org.sosy_lab.java_smt.solvers.z3

import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.ProverEnvironment

/**
 * Fix of Z3AbstractProver.addConstraint0, where created custom assumptions Z3_UNSAT_CORE_X,
 * and where **BUG** with double-ref decrease.
 */
internal fun <T> Z3AbstractProver<T>.addConstraintCustom0(constraint: BooleanFormula) {
    val z3Expr = creator.extractInfo(constraint)
    assertContraint(z3Expr)
}

fun ProverEnvironment.addConstraintCustom(constraint: BooleanFormula): Void? {
    (this as? Z3AbstractProver<*>)?.addConstraintCustom0(constraint) ?: addConstraint(constraint)
    return null
}