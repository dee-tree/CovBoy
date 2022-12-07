package com.sokolov.covboy.solvers.formulas

import com.sokolov.covboy.solvers.formulas.utils.implication
import com.sokolov.covboy.solvers.formulas.utils.uniqId
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.FormulaManager
import org.sosy_lab.java_smt.api.FormulaType

abstract class SwitchableConstraint(
    val original: BooleanFormula
    ): Constraint(original) {

    abstract val tag: String

    abstract val assumptionId: String
    abstract val assumption: BooleanFormula

    override val track: BooleanFormula
        get() = assumption

    final override val switchable: Boolean = true

    override fun toString(): String {
        return "SwitchableConstraint($original; assumption: $assumption)"
    }
}

class MutableSwitchableConstraint(
    original: BooleanFormula,
    fm: FormulaManager,
    override val tag: String = "",
    enabled: Boolean = true
): SwitchableConstraint(original) {

    override var enabled: Boolean = enabled
        private set

    override val assumptionId: String = "ass:${original.uniqId}"
    override val assumption: BooleanFormula = fm.makeVariable(FormulaType.BooleanType, assumptionId)

    override val asFormula: BooleanFormula = fm.implication(assumption, original)

    fun disable() {
        enabled = false
    }

    fun enable() {
        enabled = true
    }

    override fun equals(other: Any?): Boolean = (other is MutableSwitchableConstraint)
            && this.original == other.original

    override fun hashCode(): Int = original.uniqId.toInt()
}

fun BooleanFormula.asSwitchableConstraint(fm: FormulaManager, tag: String = "", enabled: Boolean = true): SwitchableConstraint {
    return MutableSwitchableConstraint(this, fm, tag, enabled)
}