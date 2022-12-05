package com.sokolov.covboy.solvers.formulas

import org.sosy_lab.java_smt.api.BooleanFormula

class NonSwitchableConstraint(private val original: BooleanFormula) : Constraint(original) {
    override val asFormula: BooleanFormula
        get() = original

    override val switchable: Boolean = false

    override val enabled: Boolean = true

    override fun toString(): String {
        return "NonSwitchableConstraint($original)"
    }
}

fun BooleanFormula.asNonSwitchableConstraint(): NonSwitchableConstraint = NonSwitchableConstraint(this)