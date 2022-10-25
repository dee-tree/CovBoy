package org.sosy_lab.java_smt.solvers.z3

import org.sosy_lab.common.rationals.Rational
import org.sosy_lab.java_smt.api.*
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula
import org.sosy_lab.java_smt.api.visitors.FormulaTransformationVisitor
import java.math.BigInteger

internal class Z3FormulaTransformer(
    val context: SolverContext,
    val formula: Z3Formula,
    private val newFormulaManager: FormulaManager
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

            FunctionDeclarationKind.ITE -> {
                assert(newArgs.size == 3)
                assert(functionDeclaration.argumentTypes.first().isBooleanType)
                newFormulaManager.booleanFormulaManager.ifThenElse(newArgs[0] as BooleanFormula, newArgs[1], newArgs[2])
            }

            FunctionDeclarationKind.IFF -> {
                assert(newArgs.size == 2)
                assert(functionDeclaration.argumentTypes.all { it.isBooleanType })
                newFormulaManager.booleanFormulaManager.equivalence(newArgs[0] as BooleanFormula, newArgs[1] as BooleanFormula)
            }


            // arithmetic

            FunctionDeclarationKind.DISTINCT -> {
                assert(newArgs.size > 1)
                assert(functionDeclaration.argumentTypes.all { it.isNumeralType })

                if (functionDeclaration.argumentTypes.all { it.isIntegerType })
                    newFormulaManager.integerFormulaManager.distinct(newArgs as List<IntegerFormula>)
                else
                    newFormulaManager.rationalFormulaManager.distinct(newArgs as List<NumeralFormula>)
            }

            FunctionDeclarationKind.ADD -> {
                assert(newArgs.size == 2)
                assert(functionDeclaration.argumentTypes.all { it.isNumeralType })

                if (functionDeclaration.argumentTypes.all { it.isIntegerType })
                    newFormulaManager.integerFormulaManager.add(newArgs[0] as IntegerFormula, newArgs[1] as IntegerFormula)
                else
                    newFormulaManager.rationalFormulaManager.add(newArgs[0] as NumeralFormula, newArgs[1] as NumeralFormula)
            }

            FunctionDeclarationKind.SUB -> {
                assert(newArgs.size == 2)
                assert(functionDeclaration.argumentTypes.all { it.isNumeralType })

                if (functionDeclaration.argumentTypes.all { it.isIntegerType })
                    newFormulaManager.integerFormulaManager.subtract(newArgs[0] as IntegerFormula, newArgs[1] as IntegerFormula)
                else
                    newFormulaManager.rationalFormulaManager.subtract(newArgs[0] as NumeralFormula, newArgs[1] as NumeralFormula)
            }

            FunctionDeclarationKind.DIV -> {
                assert(newArgs.size == 2)
                assert(functionDeclaration.argumentTypes.all { it.isNumeralType })

                if (functionDeclaration.argumentTypes.all { it.isIntegerType })
                    newFormulaManager.integerFormulaManager.divide(newArgs[0] as IntegerFormula, newArgs[1] as IntegerFormula)
                else
                    newFormulaManager.rationalFormulaManager.divide(newArgs[0] as NumeralFormula, newArgs[1] as NumeralFormula)
            }

            FunctionDeclarationKind.MUL -> {
                assert(newArgs.size == 2)
                assert(functionDeclaration.argumentTypes.all { it.isNumeralType })

                if (functionDeclaration.argumentTypes.all { it.isIntegerType })
                    newFormulaManager.integerFormulaManager.multiply(newArgs[0] as IntegerFormula, newArgs[1] as IntegerFormula)
                else
                    newFormulaManager.rationalFormulaManager.multiply(newArgs[0] as NumeralFormula, newArgs[1] as NumeralFormula)
            }

            FunctionDeclarationKind.MODULO -> {
                assert(newArgs.size == 2)
                assert(functionDeclaration.argumentTypes.all { it.isIntegerType })

                newFormulaManager.integerFormulaManager.modulo(newArgs[0] as IntegerFormula, newArgs[1] as IntegerFormula)

            }

            FunctionDeclarationKind.UMINUS -> {
                assert(newArgs.size == 1)
                assert(functionDeclaration.argumentTypes.first().isNumeralType)

                if (functionDeclaration.argumentTypes.first().isIntegerType)
                    newFormulaManager.integerFormulaManager.negate(newArgs[0] as IntegerFormula)
                else
                    newFormulaManager.rationalFormulaManager.negate(newArgs[0] as NumeralFormula)
            }

            FunctionDeclarationKind.LT -> {
                assert(newArgs.size == 2)
                assert(functionDeclaration.argumentTypes.all { it.isNumeralType })

                if (functionDeclaration.argumentTypes.all { it.isIntegerType })
                    newFormulaManager.integerFormulaManager.lessThan(newArgs[0] as IntegerFormula, newArgs[1] as IntegerFormula)
                else
                    newFormulaManager.rationalFormulaManager.lessThan(newArgs[0] as NumeralFormula, newArgs[1] as NumeralFormula)
            }

            FunctionDeclarationKind.LTE -> {
                assert(newArgs.size == 2)
                assert(functionDeclaration.argumentTypes.all { it.isNumeralType })

                if (functionDeclaration.argumentTypes.all { it.isIntegerType })
                    newFormulaManager.integerFormulaManager.lessOrEquals(newArgs[0] as IntegerFormula, newArgs[1] as IntegerFormula)
                else
                    newFormulaManager.rationalFormulaManager.lessOrEquals(newArgs[0] as NumeralFormula, newArgs[1] as NumeralFormula)
            }

            FunctionDeclarationKind.GT -> {
                assert(newArgs.size == 2)
                assert(functionDeclaration.argumentTypes.all { it.isNumeralType })

                if (functionDeclaration.argumentTypes.all { it.isIntegerType })
                    newFormulaManager.integerFormulaManager.greaterThan(newArgs[0] as IntegerFormula, newArgs[1] as IntegerFormula)
                else
                    newFormulaManager.rationalFormulaManager.greaterThan(newArgs[0] as NumeralFormula, newArgs[1] as NumeralFormula)
            }

            FunctionDeclarationKind.GTE -> {
                assert(newArgs.size == 2)
                assert(functionDeclaration.argumentTypes.all { it.isNumeralType })

                if (functionDeclaration.argumentTypes.all { it.isIntegerType })
                    newFormulaManager.integerFormulaManager.greaterOrEquals(newArgs[0] as IntegerFormula, newArgs[1] as IntegerFormula)
                else
                    newFormulaManager.rationalFormulaManager.greaterOrEquals(newArgs[0] as NumeralFormula, newArgs[1] as NumeralFormula)
            }

            FunctionDeclarationKind.EQ -> {
                assert(newArgs.size == 2)
                assert(functionDeclaration.argumentTypes.all { it.isNumeralType }
                        || functionDeclaration.argumentTypes.all { it.isBooleanType }
                        || functionDeclaration.argumentTypes.all { it.isArrayType }

                ) { "but actually: ${functionDeclaration.argumentTypes}" }

                when {
                    functionDeclaration.argumentTypes.all { it.isBooleanType } -> {
                        newFormulaManager.booleanFormulaManager.equivalence(
                            newArgs[0] as BooleanFormula,
                            newArgs[1] as BooleanFormula
                        )
                    }

                    functionDeclaration.argumentTypes.all { it.isArrayType } -> {
                        newFormulaManager.arrayFormulaManager.equivalence(
                            newArgs[0] as ArrayFormula<Formula, Formula>,
                            newArgs[1] as ArrayFormula<Formula, Formula>
                        )
                    }

                    functionDeclaration.argumentTypes.all { it.isIntegerType } -> {
                        newFormulaManager.integerFormulaManager.equal(
                            newArgs[0] as IntegerFormula,
                            newArgs[1] as IntegerFormula
                        )
                    }
                    else -> {
                        newFormulaManager.rationalFormulaManager.equal(
                            newArgs[0] as NumeralFormula,
                            newArgs[1] as NumeralFormula
                        )
                    }
                }
            }

            // TODO EQ_ZERO ??? 1 arg? or 2?
            FunctionDeclarationKind.EQ_ZERO -> {
                assert(newArgs.size == 1)
                assert(functionDeclaration.argumentTypes.all { it.isNumeralType })

                if (functionDeclaration.argumentTypes.all { it.isIntegerType })
                    newFormulaManager.integerFormulaManager.equal(newArgs[0] as IntegerFormula, newFormulaManager.integerFormulaManager.makeNumber(0))
                else
                    newFormulaManager.rationalFormulaManager.equal(newArgs[0] as NumeralFormula, newFormulaManager.rationalFormulaManager.makeNumber(0))
            }

            // TODO EQ_ZERO ??? 1 arg? or 2?
            FunctionDeclarationKind.GTE_ZERO -> {
                assert(newArgs.size == 1)
                assert(functionDeclaration.argumentTypes.all { it.isNumeralType })

                if (functionDeclaration.argumentTypes.all { it.isIntegerType })
                    newFormulaManager.integerFormulaManager.greaterOrEquals(newArgs[0] as IntegerFormula, newFormulaManager.integerFormulaManager.makeNumber(0))
                else
                    newFormulaManager.rationalFormulaManager.greaterOrEquals(newArgs[0] as NumeralFormula, newFormulaManager.rationalFormulaManager.makeNumber(0))
            }

            FunctionDeclarationKind.FLOOR -> {
                assert(newArgs.size == 1)
                assert(functionDeclaration.argumentTypes.first().isNumeralType)

                newFormulaManager.rationalFormulaManager.floor(newArgs[0] as NumeralFormula)
            }

            FunctionDeclarationKind.TO_REAL -> {
                assert(newArgs.size == 1)
                assert(functionDeclaration.argumentTypes.first().isNumeralType)

                // TODO: TO_REAL is it valid to return just original formula?
                newArgs.first()
            }

            // Arrays store and select

            FunctionDeclarationKind.STORE -> {
                assert(newArgs.size == 3)
                assert(functionDeclaration.argumentTypes.first().isArrayType)

                newFormulaManager.arrayFormulaManager.store(newArgs[0] as ArrayFormula<Formula, Formula>, newArgs[1], newArgs[2])
            }

            FunctionDeclarationKind.SELECT -> {
                assert(newArgs.size == 2)
                assert(functionDeclaration.argumentTypes.first().isArrayType)

                newFormulaManager.arrayFormulaManager.select(newArgs[0] as ArrayFormula<Formula, Formula>, newArgs[1])
            }

            FunctionDeclarationKind.UF -> {
                newFormulaManager.ufManager.declareAndCallUF(functionDeclaration.name, functionDeclaration.type, newArgs)
            }

            else -> TODO("Yet not implemented; kind: ${functionDeclaration.kind} of formula: $formula (${formula::class})")
        }
    }
}