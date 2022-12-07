package com.sokolov.covboy.solvers.formulas.utils

import com.sokolov.covboy.solvers.provers.secondary.fm.SecondaryBooleanFormulaManager
import org.sosy_lab.java_smt.SolverContextFactory
import org.sosy_lab.java_smt.api.*
import org.sosy_lab.java_smt.api.visitors.BooleanFormulaVisitor
import org.sosy_lab.java_smt.solvers.boolector.isBoolectorFormula
import org.sosy_lab.java_smt.solvers.boolector.nativeBoolectorTerm
import org.sosy_lab.java_smt.solvers.cvc4.isCVC4Formula
import org.sosy_lab.java_smt.solvers.princess.isPrincessFormula
import org.sosy_lab.java_smt.solvers.smtinterpol.isSmtInterpolFormula
import org.sosy_lab.java_smt.solvers.z3.isZ3Formula
import org.sosy_lab.java_smt.solvers.z3.z3Expr

fun FormulaManager.implication(a: BooleanFormula, b: BooleanFormula): BooleanFormula {
    return booleanFormulaManager.implication(a, b)
}

internal val Formula.uniqId: Long
    get() = when {
        this.isBoolectorFormula() -> this.nativeBoolectorTerm()
        this.isZ3Formula() -> this.z3Expr
        else -> this.hashCode().toLong()
    }


fun BitvectorFormulaManager.and(args: List<BitvectorFormula>): BitvectorFormula = when (args.size) {
    0 -> error("Empty args list in bv and!")
    1 -> args.first()
    2 -> and(args[0], args[1])
    else -> {
        args.subList(1, args.size).fold(args.first()) { acc, curr -> and(curr, acc) }
    }
}

fun BitvectorFormulaManager.or(args: List<BitvectorFormula>): BitvectorFormula = when (args.size) {
    0 -> error("Empty args list in bv and!")
    1 -> args.first()
    2 -> or(args[0], args[1])
    else -> {
        args.subList(1, args.size).fold(args.first()) { acc, curr -> or(curr, acc) }
    }
}

fun BitvectorFormulaManager.add(args: List<BitvectorFormula>): BitvectorFormula = when (args.size) {
    0 -> error("Empty args list in bv and!")
    1 -> args.first()
    2 -> add(args[0], args[1])
    else -> {
        args.subList(1, args.size).fold(args.first()) { acc, curr -> add(curr, acc) }
    }
}

fun BooleanFormulaManager.isBooleanLiteral(formula: Formula): Boolean = (formula as? BooleanFormula)?.let {
    isTrue(it) || isFalse(it)
} ?: false

fun BooleanFormulaManager.getBooleanLiteralValue(formula: BooleanFormula): Boolean = when {
    this.isTrue(formula) -> true
    this.isFalse(formula) -> false
    else -> throw IllegalArgumentException("$this is not boolean literal!")
}

fun BooleanFormulaManager.notOptimized(f: BooleanFormula): BooleanFormula =
    this.visit(f, object : BooleanFormulaVisitor<BooleanFormula> {

        private operator fun BooleanFormula.not() = this@notOptimized.not(f)

        override fun visitConstant(value: Boolean): BooleanFormula = this@notOptimized.makeBoolean(!value)

        override fun visitBoundVar(`var`: BooleanFormula, deBruijnIdx: Int): BooleanFormula = !`var`

        override fun visitNot(operand: BooleanFormula): BooleanFormula = operand

        override fun visitAnd(operands: List<BooleanFormula>): BooleanFormula = !this@notOptimized.and(operands)

        override fun visitOr(operands: List<BooleanFormula>): BooleanFormula = !this@notOptimized.or(operands)

        override fun visitXor(operand1: BooleanFormula, operand2: BooleanFormula): BooleanFormula =
            !this@notOptimized.xor(operand1, operand2)

        override fun visitEquivalence(operand1: BooleanFormula, operand2: BooleanFormula): BooleanFormula =
            !this@notOptimized.equivalence(operand1, operand2)

        override fun visitImplication(operand1: BooleanFormula, operand2: BooleanFormula): BooleanFormula =
            !this@notOptimized.implication(operand1, operand2)

        override fun visitIfThenElse(
            condition: BooleanFormula,
            thenFormula: BooleanFormula,
            elseFormula: BooleanFormula
        ): BooleanFormula = !this@notOptimized.ifThenElse(condition, thenFormula, elseFormula)

        override fun visitQuantifier(
            quantifier: QuantifiedFormulaManager.Quantifier,
            quantifiedAST: BooleanFormula,
            boundVars: List<Formula>,
            body: BooleanFormula
        ): BooleanFormula = !quantifiedAST

        override fun visitAtom(atom: BooleanFormula?, funcDecl: FunctionDeclaration<BooleanFormula>?): BooleanFormula =
            !atom!!
    })

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

fun SolverContextFactory.Solvers.doesSupportFormula(formula: Formula): Boolean {
    return when (this) {
        SolverContextFactory.Solvers.Z3 -> formula.isZ3Formula()
        SolverContextFactory.Solvers.CVC4 -> formula.isCVC4Formula()
        SolverContextFactory.Solvers.BOOLECTOR -> formula.isBoolectorFormula()
        SolverContextFactory.Solvers.SMTINTERPOL -> formula.isSmtInterpolFormula()
        SolverContextFactory.Solvers.PRINCESS -> formula.isPrincessFormula()
        else -> error("Unsupported solver $this")
    }
}
