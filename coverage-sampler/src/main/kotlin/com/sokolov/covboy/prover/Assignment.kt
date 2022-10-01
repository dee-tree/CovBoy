package com.sokolov.covboy.prover

import com.sokolov.covboy.smt.isFalse
import com.sokolov.covboy.smt.isTrue
import com.sokolov.covboy.smt.not
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.Formula
import org.sosy_lab.java_smt.api.FormulaManager

data class Assignment <T: Formula>(val expr: T, val value: T) {

    fun asExpr(formulaManager: FormulaManager): BooleanFormula = when {
        expr is BooleanFormula && value.isFalse(formulaManager.booleanFormulaManager) -> expr.not(formulaManager.booleanFormulaManager)
        expr is BooleanFormula && value.isTrue(formulaManager.booleanFormulaManager) -> expr
        else -> formulaManager.parse("(= ($expr) ($value))")
    }

//    override fun toString(): String = "AssignedExpr(expr = $expr, value = $value)"
}

