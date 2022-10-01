package org.sosy_lab.java_smt.solvers.z3

import org.sosy_lab.common.rationals.Rational
import org.sosy_lab.java_smt.api.*
import org.sosy_lab.java_smt.api.visitors.FormulaTransformationVisitor
import java.math.BigInteger

internal class Z3FormulaTransformer(
    val context: SolverContext,
    val formula: Z3Formula,
    val newFormulaManager: FormulaManager
) : FormulaTransformationVisitor(context.formulaManager) {

    private val z3FormulaCreator = (context.formulaManager as Z3FormulaManager).formulaCreator as Z3FormulaCreator


    fun transform(): Formula {
        return context.formulaManager.transformRecursively(formula, this)
    }


    override fun visitConstant(f: Formula, value: Any): Formula {
        val ftype = z3FormulaCreator.getFormulaType(f)

        return when {
            ftype.isBooleanType -> newFormulaManager.booleanFormulaManager.makeBoolean(value as Boolean)
            ftype.isIntegerType -> newFormulaManager.integerFormulaManager.makeNumber(value as BigInteger)
            ftype.isRationalType -> newFormulaManager.rationalFormulaManager.makeNumber(value as Rational)
            ftype.isStringType -> newFormulaManager.stringFormulaManager.makeString(value as String)
            ftype.isBitvectorType -> newFormulaManager.bitvectorFormulaManager.makeBitvector(
                (value as BigInteger).bitLength(),
                value
            )
            ftype.isFloatingPointType -> error("ooopps... floating point type") //newFormulaManager.floatingPointFormulaManager.makeNumber()
            else -> error("Unexpected type of const: $ftype")
        }
    }


    override fun visitFreeVariable(f: Formula, name: String): Formula {
        val type = z3FormulaCreator.getFormulaType(f)

        return newFormulaManager.makeVariable(type, name)
    }

    override fun visitFunction(
        f: Formula,
        newArgs: MutableList<Formula>,
        functionDeclaration: FunctionDeclaration<*>
    ): Formula {

        return when (functionDeclaration.kind) {
            // boolean kinds
            FunctionDeclarationKind.AND -> {
                assert(functionDeclaration.argumentTypes.all { it.isBooleanType })
                newFormulaManager.booleanFormulaManager.and(newArgs as MutableList<BooleanFormula>)
            }
            FunctionDeclarationKind.OR -> {
                assert(functionDeclaration.argumentTypes.all { it.isBooleanType })
                newFormulaManager.booleanFormulaManager.or(newArgs as MutableList<BooleanFormula>)
            }
            FunctionDeclarationKind.XOR -> {
                assert(newArgs.size == 2)
                assert(functionDeclaration.argumentTypes.all { it.isBooleanType })
                newFormulaManager.booleanFormulaManager.xor(
                    newArgs.first() as BooleanFormula,
                    newArgs.last() as BooleanFormula
                )
            }
            FunctionDeclarationKind.NOT -> {
                assert(newArgs.size == 1)
                assert(functionDeclaration.argumentTypes.first().isBooleanType)
                newFormulaManager.booleanFormulaManager.not(newArgs.first() as BooleanFormula)
            }

            FunctionDeclarationKind.IMPLIES -> {
                assert(newArgs.size == 2)
                assert(functionDeclaration.argumentTypes.all { it.isBooleanType })
                newFormulaManager.booleanFormulaManager.implication(newArgs.first() as BooleanFormula, newArgs.last() as BooleanFormula)
            }

            else -> TODO("Yet not implemented; formula: $formula (${formula::class})")
        }
    }
}