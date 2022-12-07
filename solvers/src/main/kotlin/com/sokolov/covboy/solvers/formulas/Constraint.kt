package com.sokolov.covboy.solvers.formulas

import org.sosy_lab.java_smt.api.BooleanFormula

abstract class Constraint(original: BooleanFormula) {

    abstract val asFormula: BooleanFormula

    abstract val switchable: Boolean

    abstract val enabled: Boolean

    abstract val track: BooleanFormula
}