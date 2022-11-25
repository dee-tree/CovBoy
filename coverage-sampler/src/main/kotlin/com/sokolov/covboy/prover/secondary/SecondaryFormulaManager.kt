package com.sokolov.covboy.prover.secondary

import org.sosy_lab.common.Appender
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
) : SecondaryFM(mapper, secondarySolver), FormulaManager /*by delegate*/ {

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

    private val _bitvectorFormulaManager: BitvectorFormulaManager by lazy {
        SecondaryBitvectorFormulaManager(
            originalFm.bitvectorFormulaManager,
            delegate.bitvectorFormulaManager,
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

    override fun getBitvectorFormulaManager(): BitvectorFormulaManager {
        return _bitvectorFormulaManager
    }


    override fun <R> visit(f: Formula, visitor: FormulaVisitor<R>): R {
        error("VISIT IN FM LOLKA")
        return originalFm.visit(f, visitor)
    }

    override fun visitRecursively(f: Formula, visitor: FormulaVisitor<TraversalProcess>) {
        error("VISIT IN FM LOLKA")
        return originalFm.visitRecursively(f, visitor)
    }

    override fun <T : Formula> transformRecursively(f: T, visitor: FormulaTransformationVisitor): T {
        error("VISIT IN FM LOLKA")
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

    override fun <T : Formula?> makeApplication(p0: FunctionDeclaration<T>, p1: MutableList<out Formula>): T {
        TODO("Not yet implemented")
    }

    override fun <T : Formula?> makeApplication(p0: FunctionDeclaration<T>, vararg p1: Formula?): T {
        TODO("Not yet implemented")
    }

    override fun getArrayFormulaManager(): ArrayFormulaManager {
        TODO("Not yet implemented")
    }

    override fun getFloatingPointFormulaManager(): FloatingPointFormulaManager {
        TODO("Not yet implemented")
    }

    override fun getUFManager(): UFManager {
        TODO("Not yet implemented")
    }

    override fun getSLFormulaManager(): SLFormulaManager {
        TODO("Not yet implemented")
    }

    override fun getQuantifiedFormulaManager(): QuantifiedFormulaManager {
        TODO("Not yet implemented")
    }

    override fun getStringFormulaManager(): StringFormulaManager {
        TODO("Not yet implemented")
    }

    override fun <T : Formula?> getFormulaType(p0: T): FormulaType<T> {
        TODO("Not yet implemented")
    }

    override fun parse(p0: String): BooleanFormula {
        TODO("Not yet implemented")
    }

    override fun dumpFormula(f: BooleanFormula): Appender {
        return if (areSecondaryFormulas(f))
            delegate.dumpFormula(f)
        else
            originalFm.dumpFormula(f)
    }

    override fun applyTactic(p0: BooleanFormula, p1: Tactic): BooleanFormula {
        TODO("Not yet implemented")
    }

    override fun <T : Formula?> simplify(p0: T): T {
        TODO("Not yet implemented")
    }

    override fun extractVariables(p0: Formula): MutableMap<String, Formula> {
        TODO("Not yet implemented")
    }

    override fun extractVariablesAndUFs(p0: Formula): MutableMap<String, Formula> {
        TODO("Not yet implemented")
    }

    override fun <T : Formula?> substitute(p0: T, p1: MutableMap<out Formula, out Formula>): T {
        TODO("Not yet implemented")
    }

    override fun translateFrom(p0: BooleanFormula, p1: FormulaManager): BooleanFormula {
        TODO("Not yet implemented")
    }

    override fun isValidName(p0: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun escape(p0: String): String {
        TODO("Not yet implemented")
    }

    override fun unescape(p0: String): String {
        TODO("Not yet implemented")
    }

    override fun <T : Formula> T.asOriginalOrNull(): T? = if (areSecondaryFormulas(this))
        mapper.findOriginal(this)
    else this
}