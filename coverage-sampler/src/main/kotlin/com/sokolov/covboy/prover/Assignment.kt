package com.sokolov.covboy.prover

import com.sokolov.covboy.smt.isFalse
import com.sokolov.covboy.smt.isTrue
import com.sokolov.covboy.smt.notOptimized
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.Formula

data class Assignment<T : Formula>(val expr: T, val value: T) {

    fun asExpr(prover: BaseProverEnvironment): BooleanFormula = when {
        expr is BooleanFormula && value.isFalse(prover.fm.booleanFormulaManager) ->
            prover.fm.booleanFormulaManager.notOptimized(expr)
        expr is BooleanFormula && value.isTrue(prover.fm.booleanFormulaManager) -> expr
        else -> prover.fm.parse("(= ($expr) ($value))")
    }

//    override fun toString(): String = "AssignedExpr(expr = $expr, value = $value)"
}

