package com.sokolov.covboy.prover

import com.sokolov.covboy.smt.implication
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.FormulaManager
import org.sosy_lab.java_smt.api.FormulaType

abstract class Constraint(original: BooleanFormula) {

    abstract val asFormula: BooleanFormula

    abstract val switchable: Boolean

    abstract val enabled: Boolean
}


class NonSwitchableConstraint(private val original: BooleanFormula) : Constraint(original) {
    override val asFormula: BooleanFormula
        get() = original

    override val switchable: Boolean = false

    override val enabled: Boolean = true

    override fun toString(): String {
        return "NonSwitchableConstraint($original)"
    }
}

abstract class SwitchableConstraint(
    val original: BooleanFormula
    ): Constraint(original) {

    abstract val tag: String

    abstract val assumptionId: String
    abstract val assumption: BooleanFormula


    override val switchable: Boolean = true

    override fun toString(): String {
        return "SwitchableConstraint($original; assumption: $assumption)"
    }
}

class MutableSwitchableConstraint(
    original: BooleanFormula,
    override val tag: String = "",
    enabled: Boolean = true,
    fm: FormulaManager
    ): SwitchableConstraint(original) {

    override var enabled: Boolean = enabled
    private set

//    val uid: String = "uid:${original.hashCode()}"
//    val uidFormula: BooleanFormula = fm.makeVariable(FormulaType.BooleanType, uid)

    override val assumptionId: String = "ass:${original.hashCode()}"
    override val assumption: BooleanFormula = fm.makeVariable(FormulaType.BooleanType, assumptionId)

    override val asFormula: BooleanFormula = fm.implication(assumption, original)


    fun disable() {
        enabled = false
    }

    fun enable() {
        enabled = true
    }

}