package com.sokolov.covboy.smt

import com.sokolov.covboy.prover.BaseProverEnvironment
import com.sokolov.covboy.prover.SecondaryProver
import org.sosy_lab.java_smt.api.*
import org.sosy_lab.java_smt.api.visitors.BooleanFormulaVisitor
import org.sosy_lab.java_smt.api.visitors.FormulaVisitor
import org.sosy_lab.java_smt.solvers.boolector.isBoolectorFormula


internal fun Formula.isBool(formulaManager: BooleanFormulaManager, value: Boolean): Boolean = (this as? BooleanFormula)
    ?.let { if (value) formulaManager.isTrue(it) else formulaManager.isFalse(it) }
    ?: false

internal fun Formula.isFalse(formulaManager: BooleanFormulaManager): Boolean = isBool(formulaManager, false)
internal fun Formula.isTrue(formulaManager: BooleanFormulaManager): Boolean = isBool(formulaManager, true)

internal fun BooleanFormula.getBooleanValue(formulaManager: BooleanFormulaManager): Boolean = when {
    this.isTrue(formulaManager) -> true
    this.isFalse(formulaManager) -> false
    else -> throw IllegalArgumentException("$this is not concrete boolean value!")
}

//internal fun BooleanFormula.not(bfm: BooleanFormulaManager): BooleanFormula = bfm.not(this)
internal fun BooleanFormula.not(bfm: BaseProverEnvironment): BooleanFormula = bfm.fm.booleanFormulaManager.not(this)

/*
internal fun BooleanFormula.not(prover: BaseProverEnvironment): BooleanFormula {
    if (this.isBoolectorFormula() && prover is SecondaryProver) {
        prover as SecondaryProver

        val originalNegExpr = (prover.getOriginalFormula(this) as? BooleanFormula)?.not(prover.z3Prover) ?: error("Lol, original ")

//        originalNegExpr?.let { originalNegExpr ->
            prover.getFromMapper(originalNegExpr)?.let {
                return it as BooleanFormula
            }

            val currentProverNeg = prover.fm.booleanFormulaManager.not(this)
            prover.putToMapper(originalNegExpr, currentProverNeg)

            return currentProverNeg
//        }
    } else {
        return notOptimized(prover.fm.booleanFormulaManager)
    }
}
*/

private fun BooleanFormula.notOptimized(bfm: BooleanFormulaManager): BooleanFormula = bfm.visit(this, object : BooleanFormulaVisitor<BooleanFormula> {

    private operator fun BooleanFormula.not() = bfm.not(this)

    override fun visitConstant(value: Boolean): BooleanFormula = bfm.makeBoolean(!value)

    override fun visitBoundVar(`var`: BooleanFormula?, deBruijnIdx: Int): BooleanFormula = !`var`!!

    override fun visitNot(operand: BooleanFormula?): BooleanFormula = operand!!

    override fun visitAnd(operands: MutableList<BooleanFormula>?): BooleanFormula = !bfm.and(operands!!)

    override fun visitOr(operands: MutableList<BooleanFormula>?): BooleanFormula = !bfm.or(operands!!)

    override fun visitXor(operand1: BooleanFormula?, operand2: BooleanFormula?): BooleanFormula = !bfm.xor(operand1!!, operand2!!)

    override fun visitEquivalence(operand1: BooleanFormula?, operand2: BooleanFormula?): BooleanFormula = !bfm.equivalence(operand1!!, operand2!!)

    override fun visitImplication(operand1: BooleanFormula?, operand2: BooleanFormula?): BooleanFormula = !bfm.implication(operand1!!, operand2!!)

    override fun visitIfThenElse(
        condition: BooleanFormula?,
        thenFormula: BooleanFormula?,
        elseFormula: BooleanFormula?
    ): BooleanFormula = !bfm.ifThenElse(condition!!, thenFormula!!, elseFormula!!)

    override fun visitQuantifier(
        quantifier: QuantifiedFormulaManager.Quantifier?,
        quantifiedAST: BooleanFormula?,
        boundVars: MutableList<Formula>?,
        body: BooleanFormula?
    ): BooleanFormula = !quantifiedAST!!

    override fun visitAtom(atom: BooleanFormula?, funcDecl: FunctionDeclaration<BooleanFormula>?): BooleanFormula = !atom!!
})

fun Formula.getFunctionDeclarationKind(fm: FormulaManager): FunctionDeclarationKind {
    return fm.visit(this, object : FormulaVisitor<FunctionDeclarationKind> {
        override fun visitFreeVariable(p0: Formula?, p1: String?): FunctionDeclarationKind = error("not a function")
        override fun visitBoundVariable(p0: Formula?, p1: Int): FunctionDeclarationKind = error("not a function")
        override fun visitConstant(p0: Formula?, p1: Any?): FunctionDeclarationKind = error("not a function")
        override fun visitQuantifier(
            p0: BooleanFormula?,
            p1: QuantifiedFormulaManager.Quantifier?,
            p2: MutableList<Formula>?,
            p3: BooleanFormula?
        ): FunctionDeclarationKind = error("not a function")

        override fun visitFunction(
            f: Formula,
            args: List<Formula>,
            funDecl: FunctionDeclaration<*>
        ): FunctionDeclarationKind = funDecl.kind
    })
}

internal fun BooleanFormulaManager.isNot(a: BooleanFormula): Boolean = this.visit(a, object : BooleanFormulaVisitor<Boolean> {
    override fun visitConstant(value: Boolean): Boolean = false

    override fun visitBoundVar(`var`: BooleanFormula?, deBruijnIdx: Int): Boolean = false

    override fun visitNot(operand: BooleanFormula?): Boolean = true

    override fun visitAnd(operands: MutableList<BooleanFormula>?): Boolean = false

    override fun visitOr(operands: MutableList<BooleanFormula>?): Boolean = false

    override fun visitXor(operand1: BooleanFormula?, operand2: BooleanFormula?): Boolean = false

    override fun visitEquivalence(operand1: BooleanFormula?, operand2: BooleanFormula?): Boolean = false

    override fun visitImplication(operand1: BooleanFormula?, operand2: BooleanFormula?): Boolean = false

    override fun visitIfThenElse(
        condition: BooleanFormula?,
        thenFormula: BooleanFormula?,
        elseFormula: BooleanFormula?
    ): Boolean = false

    override fun visitQuantifier(
        quantifier: QuantifiedFormulaManager.Quantifier?,
        quantifiedAST: BooleanFormula?,
        boundVars: MutableList<Formula>?,
        body: BooleanFormula?
    ): Boolean = false

    override fun visitAtom(atom: BooleanFormula?, funcDecl: FunctionDeclaration<BooleanFormula>?): Boolean = false
})

internal fun FormulaManager.implication(a: BooleanFormula, b: BooleanFormula): BooleanFormula {
    return booleanFormulaManager.implication(a, b)
}

internal fun BaseProverEnvironment.nand(formulas: List<BooleanFormula>): BooleanFormula {
    return if (formulas.size == 1) formulas.first().not(this)
    else fm.booleanFormulaManager.and(formulas).not(this)
}


internal fun Formula.isCertainBool(fm: BooleanFormulaManager): Boolean = (this as? BooleanFormula)?.let {
    fm.isTrue(it) || fm.isFalse(it)
} ?: false