package com.sokolov.covboy.solvers.formulas

import com.sokolov.covboy.solvers.formulas.utils.uniqId
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.FormulaManager

class NonSwitchableConstraint(
    private val original: BooleanFormula,
    fm: FormulaManager
    ) : Constraint(original) {
    override val asFormula: BooleanFormula
        get() = original

    override val switchable: Boolean = false

    override val enabled: Boolean = true

    override val track: BooleanFormula = fm.booleanFormulaManager.makeVariable("track:${original.uniqId}")

    override fun toString(): String {
        return "NonSwitchableConstraint($original)"
    }
}

fun BooleanFormula.asNonSwitchableConstraint(fm: FormulaManager): NonSwitchableConstraint = NonSwitchableConstraint(this, fm)