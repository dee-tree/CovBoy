package com.sokolov.covboy.prover

import com.sokolov.covboy.smt.implication
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.FormulaType
import org.sosy_lab.java_smt.api.ProverEnvironment

class Assertion(
    private val prover: IProver,
    val expr: BooleanFormula,
    val tag: String = "",
    var onAssertionChanged: ((AssertionState) -> Unit)? = null
) {
    val uid = "uid:${expr.hashCode()}"
    val uidExpr: BooleanFormula = prover.context.formulaManager.makeVariable(FormulaType.BooleanType, uid)

    val assumptionName = "cond:${expr.hashCode()}"
    val conditionExpr: BooleanFormula = prover.context.formulaManager.makeVariable(FormulaType.BooleanType, assumptionName)

    val assumption: BooleanFormula
        get() = conditionExpr

    var enabled = true
        private set

    private val asFormula = prover.context.formulaManager.implication(assumption, expr)

    fun put(prover: ProverEnvironment): Assertion = apply {
        prover.addConstraint(asFormula)
        onAssertionChanged?.invoke(AssertionState(uid, enabled))
    }

    fun disable() {
        enabled = false
        onAssertionChanged?.invoke(AssertionState(uid, enabled))
    }

    fun enable() {
        enabled = true
        onAssertionChanged?.invoke(AssertionState(uid, enabled))
    }

    override fun toString(): String = "AssertionInfo(uid = $uid, enabled = $enabled, expression hash = ${expr.hashCode()}, tag = $tag)"
}

data class AssertionState(
    val uid: String,
    val enabled: Boolean
)