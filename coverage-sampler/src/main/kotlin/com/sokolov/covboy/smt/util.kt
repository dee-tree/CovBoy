package com.sokolov.covboy.smt

import com.sokolov.covboy.prover.secondary.SecondaryBooleanFormulaManager
import org.sosy_lab.java_smt.SolverContextFactory.Solvers
import org.sosy_lab.java_smt.api.*
import org.sosy_lab.java_smt.api.visitors.BooleanFormulaVisitor
import org.sosy_lab.java_smt.api.visitors.FormulaVisitor
import org.sosy_lab.java_smt.solvers.boolector.isBoolectorFormula
import org.sosy_lab.java_smt.solvers.cvc4.isCVC4Formula
import org.sosy_lab.java_smt.solvers.princess.isPrincessFormula
import org.sosy_lab.java_smt.solvers.smtinterpol.isSmtInterpolFormula
import org.sosy_lab.java_smt.solvers.z3.isZ3Formula


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

internal fun BooleanFormulaManager.notOptimized(f: BooleanFormula): BooleanFormula =
    this.visit(f, object : BooleanFormulaVisitor<BooleanFormula> {

        private operator fun BooleanFormula.not() = this@notOptimized.not(f)

        override fun visitConstant(value: Boolean): BooleanFormula = this@notOptimized.makeBoolean(!value)

        override fun visitBoundVar(`var`: BooleanFormula?, deBruijnIdx: Int): BooleanFormula = !`var`!!

        override fun visitNot(operand: BooleanFormula?): BooleanFormula = operand!!

        override fun visitAnd(operands: MutableList<BooleanFormula>?): BooleanFormula = !this@notOptimized.and(operands!!)

        override fun visitOr(operands: MutableList<BooleanFormula>?): BooleanFormula = !this@notOptimized.or(operands!!)

        override fun visitXor(operand1: BooleanFormula?, operand2: BooleanFormula?): BooleanFormula =
            !this@notOptimized.xor(operand1!!, operand2!!)

        override fun visitEquivalence(operand1: BooleanFormula?, operand2: BooleanFormula?): BooleanFormula =
            !this@notOptimized.equivalence(operand1!!, operand2!!)

        override fun visitImplication(operand1: BooleanFormula?, operand2: BooleanFormula?): BooleanFormula =
            !this@notOptimized.implication(operand1!!, operand2!!)

        override fun visitIfThenElse(
            condition: BooleanFormula?,
            thenFormula: BooleanFormula?,
            elseFormula: BooleanFormula?
        ): BooleanFormula = !this@notOptimized.ifThenElse(condition!!, thenFormula!!, elseFormula!!)

        override fun visitQuantifier(
            quantifier: QuantifiedFormulaManager.Quantifier?,
            quantifiedAST: BooleanFormula?,
            boundVars: MutableList<Formula>?,
            body: BooleanFormula?
        ): BooleanFormula = !quantifiedAST!!

        override fun visitAtom(atom: BooleanFormula?, funcDecl: FunctionDeclaration<BooleanFormula>?): BooleanFormula =
            !atom!!
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


internal fun BooleanFormulaManager.isNot(a: BooleanFormula): Boolean {
    return if (this is SecondaryBooleanFormulaManager) (this as SecondaryBooleanFormulaManager).isNot(a)
    else isNotVisit(a)
}

internal fun BooleanFormulaManager.isNotVisit(a: BooleanFormula): Boolean =
    this.visit(a, object : BooleanFormulaVisitor<Boolean> {
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

internal fun BooleanFormulaManager.nand(formulas: List<BooleanFormula>): BooleanFormula = when {
    formulas.isEmpty() -> error("empty args of nand")
    formulas.size == 1 -> notOptimized(formulas.first())
    else -> not(and(formulas))
}


internal fun Formula.isCertainBool(fm: BooleanFormulaManager): Boolean = (this as? BooleanFormula)?.let {
    fm.isTrue(it) || fm.isFalse(it)
} ?: false

fun Solvers.isFormulaSupported(formula: Formula): Boolean {
    return when (this) {
        Solvers.Z3 -> formula.isZ3Formula()
        Solvers.CVC4 -> formula.isCVC4Formula()
        Solvers.BOOLECTOR -> formula.isBoolectorFormula()
        Solvers.SMTINTERPOL -> formula.isSmtInterpolFormula()
        Solvers.PRINCESS -> formula.isPrincessFormula()
        else -> error("Unsupported solver $this")
    }
}
