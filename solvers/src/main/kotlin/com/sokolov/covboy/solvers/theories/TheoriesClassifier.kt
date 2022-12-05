package com.sokolov.covboy.solvers.theories

import org.sosy_lab.java_smt.api.*
import org.sosy_lab.java_smt.api.visitors.FormulaVisitor

/*
Source: org.sosy_lab.java_smt.example.FormulaClassifier => Classifier
 */
class TheoriesClassifier(private val formulas: Collection<Formula>, private val fm: FormulaManager) : FormulaVisitor<Int> {

    private var hasUFs = false
    private var hasQuantifiers = false

    private var hasFloats = false
    private var hasInts = false
    private var hasReals = false
    private var hasBVs = false
    private var hasArrays = false
    private var linearArithmetic = false
    private var nonLinearArithmetic = false

    fun process(): Set<Theories> {
        formulas.forEach { constraint ->
            fm.visit(constraint, this)
        }

        return toTheoriesSet()
    }

    private fun Formula.checkType() {
        val type = fm.getFormulaType(this)
        if (type.isIntegerType) {
            hasInts = true
        }
        if (type.isRationalType) {
            hasReals = true
        }
        if (type.isFloatingPointType) {
            hasFloats = true
        }
        if (type.isBitvectorType) {
            hasBVs = true
        }
        if (type.isArrayType) {
            hasArrays = true
        }
    }

    override fun visitFreeVariable(f: Formula, name: String): Int {
        f.checkType()
        return 1
    }

    override fun visitBoundVariable(f: Formula, deBruijnIdx: Int): Int {
        f.checkType()
        return 1
    }

    override fun visitConstant(f: Formula, value: Any): Int {
        f.checkType()
        return 0
    }

    override fun visitFunction(f: Formula, args: List<Formula>, declaration: FunctionDeclaration<*>): Int {
        if (declaration.kind == FunctionDeclarationKind.UF) {
            hasUFs = true
        }
        f.checkType()

        var numNonConstantArgs = 0
        var allArgLevel = 0

        args.forEach { arg ->
            val argLevel = fm.visit(arg, this)
            if (argLevel >= 1)
                numNonConstantArgs++

            allArgLevel = allArgLevel.coerceAtLeast(argLevel)
        }

        if (
            (declaration.kind == FunctionDeclarationKind.MUL
                    || declaration.kind == FunctionDeclarationKind.BV_MUL
                    || declaration.kind == FunctionDeclarationKind.DIV
                    || declaration.kind == FunctionDeclarationKind.BV_UDIV
                    || declaration.kind == FunctionDeclarationKind.BV_SDIV
                    || declaration.kind == FunctionDeclarationKind.MODULO
                    || declaration.kind == FunctionDeclarationKind.BV_UREM
                    || declaration.kind == FunctionDeclarationKind.BV_SREM)
            && numNonConstantArgs >= 2
        ) {
            nonLinearArithmetic = true
            return allArgLevel + 1
        } else {
            if (declaration.type.isBooleanType) {
                if (
                    declaration.kind == FunctionDeclarationKind.LT
                    || declaration.kind == FunctionDeclarationKind.LTE
                    || declaration.kind == FunctionDeclarationKind.GT
                    || declaration.kind == FunctionDeclarationKind.GTE
                ) {
                    args.forEach { arg ->
                        val type = fm.getFormulaType(arg)
                        if (type.isIntegerType || type.isRationalType)
                            linearArithmetic = true
                    }
                }
                return 0
            } else {
                if (declaration.kind != FunctionDeclarationKind.UF) {
                    linearArithmetic = true
                }
                return allArgLevel
            }
        }
    }

    override fun visitQuantifier(
        f: BooleanFormula,
        quantifier: QuantifiedFormulaManager.Quantifier,
        boundVariables: List<Formula>,
        body: BooleanFormula
    ): Int {
        hasQuantifiers = true
        f.checkType()
        return fm.visit(body, this)
    }

    private fun toTheoriesSet(): Set<Theories> = buildSet {
        if (hasUFs) add(Theories.UF)
        if (hasQuantifiers) add(Theories.QUANTIFIER)

        if (hasFloats) add(Theories.FLOAT)
        if (hasInts) add(Theories.INTEGER)
        if (hasReals) add(Theories.RATIONAL)
        if (hasBVs) add(Theories.BITVECTOR)
        if (hasArrays) add(Theories.ARRAY)
    }
}

fun FormulaManager.theories(formulas: Collection<Formula>): Set<Theories> = TheoriesClassifier(formulas, this).process()
fun FormulaManager.theories(formula: Formula): Set<Theories> = TheoriesClassifier(listOf(formula), this).process()