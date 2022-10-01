package com.sokolov.smt

import org.sosy_lab.java_smt.api.*
import org.sosy_lab.java_smt.api.visitors.BooleanFormulaVisitor


internal fun Formula.isBool(formulaManager: BooleanFormulaManager, value: Boolean): Boolean = (this as? BooleanFormula)
    ?.let { if (value) formulaManager.isTrue(it) else formulaManager.isFalse(it) }
    ?: false

internal fun Formula.isFalse(formulaManager: BooleanFormulaManager): Boolean = isBool(formulaManager, false)
internal fun Formula.isTrue(formulaManager: BooleanFormulaManager): Boolean = isBool(formulaManager, true)


internal fun BooleanFormula.not(bfm: BooleanFormulaManager): BooleanFormula = bfm.not(this)


/*internal fun BooleanFormula.not(bfm: BooleanFormulaManager): BooleanFormula = bfm.visit(this, object : BooleanFormulaVisitor<BooleanFormula> {

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
})*/

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

internal fun Formula.isCertainBool(fm: BooleanFormulaManager): Boolean = (this as? BooleanFormula)?.let {
    fm.isTrue(it) || fm.isFalse(it)
} ?: false