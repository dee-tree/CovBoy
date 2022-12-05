package com.sokolov.covboy.solvers.provers.secondary.fm

import com.sokolov.covboy.solvers.provers.secondary.FormulaMapper
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
) : SecondaryFM(mapper, secondarySolver, originalFm, delegate), FormulaManager /*by delegate*/ {

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
        return originalFm.visit(f.asOriginal(), visitor)
    }

    override fun visitRecursively(f: Formula, visitor: FormulaVisitor<TraversalProcess>) {
        return originalFm.visitRecursively(f.asOriginal(), visitor)
    }

    override fun <T : Formula> transformRecursively(f: T, visitor: FormulaTransformationVisitor): T {
        return originalFm.transformRecursively(f.asOriginal(), visitor)
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

    override fun <T : Formula?> makeApplication(decl: FunctionDeclaration<T>, args: List<Formula>): T {
        return originalFm.makeApplication(decl, args.map { it.asOriginal() })
    }

    override fun <T : Formula?> makeApplication(decl: FunctionDeclaration<T>, vararg args: Formula): T {
        return originalFm.makeApplication(decl, args.map { it.asOriginal() })
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

    override fun <T : Formula> getFormulaType(f: T): FormulaType<T> {
        return originalFm.getFormulaType(f.asOriginal())
    }

    override fun parse(f: String): BooleanFormula {
        return mapper.toSecondary(originalFm.parse(f))
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

    override fun <T : Formula> simplify(f: T): T {
        return mapper.toSecondary(originalFm.simplify(f))
    }

    override fun extractVariables(f: Formula): Map<String, Formula> {
        return originalFm.extractVariables(f).map { it.key to mapper.toSecondary(it.value) }.toMap()
    }

    override fun extractVariablesAndUFs(f: Formula): Map<String, Formula> {
        return originalFm.extractVariablesAndUFs(f).map { it.key to mapper.toSecondary(it.value) }.toMap()
    }

    override fun <T : Formula> substitute(f: T, changes: Map<out Formula, Formula>): T {
        return mapper.toSecondary(originalFm.substitute(f.asOriginal(), changes.map { mapper.toSecondary(it.key) to mapper.toSecondary(it.value) }.toMap()))
    }

    override fun translateFrom(f: BooleanFormula, fm: FormulaManager): BooleanFormula {
        TODO("Not yet implemented")
    }

    override fun isValidName(name: String): Boolean {
        return delegate.isValidName(name)
    }

    override fun escape(str: String): String {
        return delegate.escape(str)
    }

    override fun unescape(str: String): String {
        return delegate.unescape(str)
    }

    override fun <T : Formula> T.asOriginalOrNull(): T? = if (areSecondaryFormulas(this))
        mapper.findOriginal(this)
    else this
}