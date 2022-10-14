package com.sokolov.covboy.prover

import com.sokolov.covboy.prover.assertions.AssertionListener
import com.sokolov.covboy.smt.implication
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.FormulaManager
import org.sosy_lab.java_smt.api.FormulaType
import org.sosy_lab.java_smt.api.ProverEnvironment

class Assertion(
    fm: FormulaManager,
    val expr: BooleanFormula,
    val tag: String = "",
    var assertionListener: AssertionListener? = null
) {
    val uid = "uid:${expr.hashCode()}"
    val uidExpr: BooleanFormula = fm.makeVariable(FormulaType.BooleanType, uid)

    val assumptionName = "cond:${expr.hashCode()}"
    val conditionExpr: BooleanFormula = fm.makeVariable(FormulaType.BooleanType, assumptionName)

    val assumption: BooleanFormula
        get() = conditionExpr

    var enabled = true
        private set

    private val asFormula = fm.implication(assumption, expr)

    fun put(prover: ProverEnvironment): Assertion = apply {
        prover.addConstraint(asFormula)
        assertionListener?.onAssertionEnabled(this)
    }

    fun disable() {
        enabled = false
        assertionListener?.onAssertionDisabled(this)
    }

    fun enable() {
        enabled = true
        assertionListener?.onAssertionEnabled(this)
    }

    override fun toString(): String = "AssertionInfo(uid = $uid, enabled = $enabled, expression hash = ${expr.hashCode()}, tag = $tag)"
}

data class AssertionState(
    val uid: String,
    val enabled: Boolean
)