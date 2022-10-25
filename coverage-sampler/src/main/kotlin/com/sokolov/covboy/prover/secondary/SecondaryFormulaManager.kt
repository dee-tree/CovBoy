package com.sokolov.covboy.prover.secondary

import org.sosy_lab.java_smt.SolverContextFactory.Solvers
import org.sosy_lab.java_smt.api.*
import org.sosy_lab.java_smt.api.FormulaType.BitvectorType
import org.sosy_lab.java_smt.api.FormulaType.FloatingPointType
import org.sosy_lab.java_smt.api.visitors.FormulaTransformationVisitor
import org.sosy_lab.java_smt.api.visitors.FormulaVisitor
import org.sosy_lab.java_smt.api.visitors.TraversalProcess

class SecondaryFormulaManager(
    private val originalFm: FormulaManager,
    private val delegate: FormulaManager,

    secondarySolver: Solvers,
    mapper: FormulaMapper
) : SecondaryFM(mapper, secondarySolver), FormulaManager by delegate {

    private val booleanFormulaManager: BooleanFormulaManager = SecondaryBooleanFormulaManager(
        originalFm.booleanFormulaManager,
        delegate.booleanFormulaManager,
        this
    )

    private val _integerFormulaManager: IntegerFormulaManager by lazy {
        SecondaryIntegerFormulaManager(
            originalFm.integerFormulaManager,
            delegate.integerFormulaManager,
            this
        )
    }

    private val _rationalFormulaManager: RationalFormulaManager by lazy {
        SecondaryRationalFormulaManager(
            originalFm.rationalFormulaManager,
            delegate.rationalFormulaManager,
            this
        )
    }

    override fun getBooleanFormulaManager(): BooleanFormulaManager {
        return booleanFormulaManager
    }

    override fun getIntegerFormulaManager(): IntegerFormulaManager {
        return _integerFormulaManager
    }

    override fun getRationalFormulaManager(): RationalFormulaManager {
        return _rationalFormulaManager
    }


    override fun <R> visit(f: Formula, visitor: FormulaVisitor<R>): R {
        return originalFm.visit(f, visitor)
    }

    override fun visitRecursively(f: Formula, visitor: FormulaVisitor<TraversalProcess>) {
        return originalFm.visitRecursively(f, visitor)
    }

    override fun <T : Formula> transformRecursively(f: T, visitor: FormulaTransformationVisitor): T {
        return originalFm.transformRecursively(f, visitor)
    }

    override fun <T : Formula> makeVariable(type: FormulaType<T>, name: String): T {
        return when {
            type.isBooleanType -> booleanFormulaManager.makeVariable(name)
            type.isIntegerType -> integerFormulaManager.makeVariable(name)
            type.isRationalType -> rationalFormulaManager.makeVariable(name)
            type.isStringType -> stringFormulaManager.makeVariable(name)
            type.isBitvectorType -> bitvectorFormulaManager.makeVariable(type as BitvectorType, name)
            type.isFloatingPointType -> floatingPointFormulaManager.makeVariable(name, type as FloatingPointType)
            type.isArrayType -> arrayFormulaManager.makeArray(name, type as FormulaType.ArrayFormulaType<*, *>)
            else -> error("Unknown formula type")
        } as T
    }

    override fun <T : Formula> T.asOriginalOrNull(): T? = if (areSecondaryFormulas(this))
        mapper.findOriginal(this)
    else this
}