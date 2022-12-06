package org.sosy_lab.java_smt.solvers.z3

import com.microsoft.z3.Native
import org.sosy_lab.java_smt.api.*
import org.sosy_lab.java_smt.api.visitors.FormulaVisitor
import org.sosy_lab.java_smt.basicimpl.FunctionDeclarationImpl
import org.sosy_lab.java_smt.solvers.boolector.getDeclaredMethodRecursively
import org.sosy_lab.java_smt.solvers.z3.Z3Formula.Z3BooleanFormula
import java.io.File

fun Formula.isZ3Formula() = this is Z3Formula

val Formula.z3Expr: Long
    get() = (this as Z3Formula).formulaInfo

internal fun BooleanFormula.isZ3Not(fm: FormulaManager): Boolean {
    val z3f = (this as? Z3BooleanFormula) ?: return false

    return z3f.getDeclarationKind(fm) == FunctionDeclarationKind.NOT
}

private fun Z3BooleanFormula.getDeclarationKind(fm: FormulaManager): FunctionDeclarationKind {
    val fc = ((fm as Z3FormulaManager).formulaCreator as Z3FormulaCreator)
    val method = fc::class.java.getDeclaredMethodRecursively("getDeclarationKind", Long::class.java)
    method.isAccessible = true

    return method.invoke(fc, this.z3Expr) as FunctionDeclarationKind
}

private fun Formula.getFunArgs(fm: FormulaManager): List<Formula> {
    return fm.visit(this, object : FormulaVisitor<List<Formula>> {
        override fun visitFreeVariable(p0: Formula?, p1: String?): List<Formula> = emptyList()

        override fun visitBoundVariable(p0: Formula?, p1: Int): List<Formula> = emptyList()

        override fun visitConstant(p0: Formula?, p1: Any?): List<Formula> = emptyList()

        override fun visitFunction(
            f: Formula,
            args: List<Formula>,
            funDecl: FunctionDeclaration<*>
        ): List<Formula> = args

        override fun visitQuantifier(
            p0: BooleanFormula?,
            p1: QuantifiedFormulaManager.Quantifier?,
            p2: MutableList<Formula>?,
            p3: BooleanFormula?
        ): List<Formula> = emptyList()
    })
}

fun ProverEnvironment.z3FromFile(file: File) {
    Native.solverFromFile(z3Context(), z3Solver(), file.absolutePath)
}

fun ProverEnvironment.z3Assertions(): List<Formula> {
    val assertions = Native.solverGetAssertions(z3Context(), z3Solver())
    Native.astVectorIncRef(z3Context(), assertions)

    try {
        return List(Native.astVectorSize(z3Context(), assertions)) { i ->
            val ast = Native.astVectorGet(z3Context(), assertions, i)
            z3FormulaCreator().encapsulate(z3FormulaCreator().getFormulaType(ast), ast)
        }

    } catch (e: Exception) {
        System.err.println(e)
    } finally {
        Native.astVectorDecRef(z3Context(), assertions)
    }

    return emptyList()
}

fun FormulaManager.z3getIntParam(fDecl: FunctionDeclaration<*>, idx: Int): Int {
    val creator = (this as Z3FormulaManager).formulaCreator
    fDecl as FunctionDeclarationImpl<*, *>

    return Native.getDeclIntParameter(creator.env, fDecl.solverDeclaration as Long, idx)
}

internal fun FormulaManager.getIntParam(fDecl: FunctionDeclaration<*>, idx: Int): Int {
    return when (this) {
        is Z3FormulaManager -> z3getIntParam(fDecl, idx)
        else -> error("Unknown fm $this")
    }
}