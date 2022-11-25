package org.sosy_lab.java_smt.solvers.z3

import org.sosy_lab.common.rationals.Rational
import org.sosy_lab.java_smt.api.*
import org.sosy_lab.java_smt.api.FormulaType.BitvectorType
import org.sosy_lab.java_smt.api.FormulaType.FloatingPointType
import org.sosy_lab.java_smt.api.NumeralFormula.IntegerFormula
import org.sosy_lab.java_smt.api.visitors.FormulaTransformationVisitor
import org.sosy_lab.java_smt.basicimpl.add
import org.sosy_lab.java_smt.basicimpl.and
import org.sosy_lab.java_smt.basicimpl.or
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
            ftype.isBitvectorType -> {
                val size = (ftype as BitvectorType).size
                when(value) {
                    is BigInteger -> newFormulaManager.bitvectorFormulaManager.makeBitvector(size, value)
                    is Long -> newFormulaManager.bitvectorFormulaManager.makeBitvector(size, value)
                    is IntegerFormula -> newFormulaManager.bitvectorFormulaManager.makeBitvector(size, value)
                    else -> error("can't make bitvector from type ${value::class}: $value")
                }
            }
            ftype.isIntegerType -> newFormulaManager.integerFormulaManager.makeNumber(value as BigInteger)
            ftype.isRationalType -> newFormulaManager.rationalFormulaManager.makeNumber(value as Rational)
            ftype.isStringType -> newFormulaManager.stringFormulaManager.makeString(value as String)
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
                check(functionDeclaration.argumentTypes.all { it.isBooleanType })
                newFormulaManager.booleanFormulaManager.and(newArgs as List<BooleanFormula>)
            }
            FunctionDeclarationKind.OR -> {
                check(functionDeclaration.argumentTypes.all { it.isBooleanType })
                newFormulaManager.booleanFormulaManager.or(newArgs as List<BooleanFormula>)
            }
            FunctionDeclarationKind.XOR -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isBooleanType })
                newFormulaManager.booleanFormulaManager.xor(
                    newArgs.first() as BooleanFormula,
                    newArgs.last() as BooleanFormula
                )
            }
            FunctionDeclarationKind.NOT -> {
                check(newArgs.size == 1)
                check(functionDeclaration.argumentTypes.first().isBooleanType)
                newFormulaManager.booleanFormulaManager.not(newArgs.first() as BooleanFormula)
            }

            FunctionDeclarationKind.IMPLIES -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isBooleanType })
                newFormulaManager.booleanFormulaManager.implication(newArgs.first() as BooleanFormula, newArgs.last() as BooleanFormula)
            }

            FunctionDeclarationKind.ITE -> {
                check(newArgs.size == 3)
                check(functionDeclaration.argumentTypes.first().isBooleanType)
                newFormulaManager.booleanFormulaManager.ifThenElse(newArgs[0] as BooleanFormula, newArgs[1], newArgs[2])
            }

            FunctionDeclarationKind.IFF -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isBooleanType })
                newFormulaManager.booleanFormulaManager.equivalence(newArgs[0] as BooleanFormula, newArgs[1] as BooleanFormula)
            }


            // arithmetic

            FunctionDeclarationKind.DISTINCT -> {
                check(newArgs.size > 1)
                check(functionDeclaration.argumentTypes.all { it.isNumeralType }
                        || functionDeclaration.argumentTypes.all { it.isBooleanType }
                        || functionDeclaration.argumentTypes.all { it.isArrayType }
                        || functionDeclaration.argumentTypes.all { it.isBitvectorType })

                if (functionDeclaration.argumentTypes.all { it.isNumeralType }) {
                    if (functionDeclaration.argumentTypes.all { it.isIntegerType })
                        newFormulaManager.integerFormulaManager.distinct(newArgs as List<IntegerFormula>)
                    else
                        newFormulaManager.rationalFormulaManager.distinct(newArgs as List<NumeralFormula>)
                } else
                    if (functionDeclaration.argumentTypes.all { it.isBitvectorType })
                        newFormulaManager.bitvectorFormulaManager.distinct(newArgs as List<BitvectorFormula>)
                else TODO("Distinct on types ${functionDeclaration.argumentTypes}")
            }

            FunctionDeclarationKind.ADD -> {
                check(newArgs.size > 1)
                check(functionDeclaration.argumentTypes.all { it.isNumeralType })

                if (functionDeclaration.argumentTypes.all { it.isIntegerType })
                    newFormulaManager.integerFormulaManager.sum(newArgs as List<IntegerFormula>)
                else
                    newFormulaManager.rationalFormulaManager.sum(newArgs as List<NumeralFormula>)
            }

            FunctionDeclarationKind.SUB -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isNumeralType })

                if (functionDeclaration.argumentTypes.all { it.isIntegerType })
                    newFormulaManager.integerFormulaManager.subtract(newArgs[0] as IntegerFormula, newArgs[1] as IntegerFormula)
                else
                    newFormulaManager.rationalFormulaManager.subtract(newArgs[0] as NumeralFormula, newArgs[1] as NumeralFormula)
            }

            FunctionDeclarationKind.DIV -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isNumeralType })

                if (functionDeclaration.argumentTypes.all { it.isIntegerType })
                    newFormulaManager.integerFormulaManager.divide(newArgs[0] as IntegerFormula, newArgs[1] as IntegerFormula)
                else
                    newFormulaManager.rationalFormulaManager.divide(newArgs[0] as NumeralFormula, newArgs[1] as NumeralFormula)
            }

            FunctionDeclarationKind.MUL -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isNumeralType })

                if (functionDeclaration.argumentTypes.all { it.isIntegerType })
                    newFormulaManager.integerFormulaManager.multiply(newArgs[0] as IntegerFormula, newArgs[1] as IntegerFormula)
                else
                    newFormulaManager.rationalFormulaManager.multiply(newArgs[0] as NumeralFormula, newArgs[1] as NumeralFormula)
            }

            FunctionDeclarationKind.MODULO -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isIntegerType })

                newFormulaManager.integerFormulaManager.modulo(newArgs[0] as IntegerFormula, newArgs[1] as IntegerFormula)

            }

            FunctionDeclarationKind.UMINUS -> {
                check(newArgs.size == 1)
                check(functionDeclaration.argumentTypes.first().isNumeralType)

                if (functionDeclaration.argumentTypes.first().isIntegerType)
                    newFormulaManager.integerFormulaManager.negate(newArgs[0] as IntegerFormula)
                else
                    newFormulaManager.rationalFormulaManager.negate(newArgs[0] as NumeralFormula)
            }

            FunctionDeclarationKind.LT -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isNumeralType })

                if (functionDeclaration.argumentTypes.all { it.isIntegerType })
                    newFormulaManager.integerFormulaManager.lessThan(newArgs[0] as IntegerFormula, newArgs[1] as IntegerFormula)
                else
                    newFormulaManager.rationalFormulaManager.lessThan(newArgs[0] as NumeralFormula, newArgs[1] as NumeralFormula)
            }

            FunctionDeclarationKind.LTE -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isNumeralType })

                if (functionDeclaration.argumentTypes.all { it.isIntegerType })
                    newFormulaManager.integerFormulaManager.lessOrEquals(newArgs[0] as IntegerFormula, newArgs[1] as IntegerFormula)
                else
                    newFormulaManager.rationalFormulaManager.lessOrEquals(newArgs[0] as NumeralFormula, newArgs[1] as NumeralFormula)
            }

            FunctionDeclarationKind.GT -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isNumeralType })

                if (functionDeclaration.argumentTypes.all { it.isIntegerType })
                    newFormulaManager.integerFormulaManager.greaterThan(newArgs[0] as IntegerFormula, newArgs[1] as IntegerFormula)
                else
                    newFormulaManager.rationalFormulaManager.greaterThan(newArgs[0] as NumeralFormula, newArgs[1] as NumeralFormula)
            }

            FunctionDeclarationKind.GTE -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isNumeralType })

                if (functionDeclaration.argumentTypes.all { it.isIntegerType })
                    newFormulaManager.integerFormulaManager.greaterOrEquals(newArgs[0] as IntegerFormula, newArgs[1] as IntegerFormula)
                else
                    newFormulaManager.rationalFormulaManager.greaterOrEquals(newArgs[0] as NumeralFormula, newArgs[1] as NumeralFormula)
            }

            FunctionDeclarationKind.EQ -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isNumeralType }
                        || functionDeclaration.argumentTypes.all { it.isBooleanType }
                        || functionDeclaration.argumentTypes.all { it.isArrayType }
                        || functionDeclaration.argumentTypes.all { it.isBitvectorType }

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

                    functionDeclaration.argumentTypes.all { it.isBitvectorType } -> {
                        newFormulaManager.bitvectorFormulaManager.equal(
                            newArgs[0] as BitvectorFormula,
                            newArgs[1] as BitvectorFormula
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

            FunctionDeclarationKind.EQ_ZERO -> {
                check(newArgs.size == 1)
                check(functionDeclaration.argumentTypes.all { it.isNumeralType })

                if (functionDeclaration.argumentTypes.all { it.isIntegerType })
                    newFormulaManager.integerFormulaManager.equal(newArgs[0] as IntegerFormula, newFormulaManager.integerFormulaManager.makeNumber(0))
                else
                    newFormulaManager.rationalFormulaManager.equal(newArgs[0] as NumeralFormula, newFormulaManager.rationalFormulaManager.makeNumber(0))
            }

            FunctionDeclarationKind.GTE_ZERO -> {
                check(newArgs.size == 1)
                check(functionDeclaration.argumentTypes.all { it.isNumeralType })

                if (functionDeclaration.argumentTypes.all { it.isIntegerType })
                    newFormulaManager.integerFormulaManager.greaterOrEquals(newArgs[0] as IntegerFormula, newFormulaManager.integerFormulaManager.makeNumber(0))
                else
                    newFormulaManager.rationalFormulaManager.greaterOrEquals(newArgs[0] as NumeralFormula, newFormulaManager.rationalFormulaManager.makeNumber(0))
            }

            FunctionDeclarationKind.FLOOR -> {
                check(newArgs.size == 1)
                check(functionDeclaration.argumentTypes.first().isNumeralType)

                newFormulaManager.rationalFormulaManager.floor(newArgs[0] as NumeralFormula)
            }

            FunctionDeclarationKind.TO_REAL -> {
                check(newArgs.size == 1)
                check(functionDeclaration.argumentTypes.first().isNumeralType)

                // TODO: TO_REAL is it valid to return just original formula?
                newArgs.first()
            }

            // Arrays store and select

            FunctionDeclarationKind.STORE -> {
                check(newArgs.size == 3)
                check(functionDeclaration.argumentTypes.first().isArrayType)

                newFormulaManager.arrayFormulaManager.store(newArgs[0] as ArrayFormula<Formula, Formula>, newArgs[1], newArgs[2])
            }

            FunctionDeclarationKind.SELECT -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.first().isArrayType)

                newFormulaManager.arrayFormulaManager.select(newArgs[0] as ArrayFormula<Formula, Formula>, newArgs[1])
            }

            // UF

            FunctionDeclarationKind.UF -> {
                newFormulaManager.ufManager.declareAndCallUF(functionDeclaration.name, functionDeclaration.type, newArgs)
            }

            // Bitvector logical

            FunctionDeclarationKind.BV_AND -> {
                check(newArgs.size > 1)
                check(functionDeclaration.argumentTypes.all { it.isBitvectorType })

                newFormulaManager.bitvectorFormulaManager.and(newArgs as List<BitvectorFormula>)
            }

            FunctionDeclarationKind.BV_OR -> {
                check(newArgs.size > 1)
                check(functionDeclaration.argumentTypes.all { it.isBitvectorType })

                newFormulaManager.bitvectorFormulaManager.or(newArgs as List<BitvectorFormula>)
            }

            FunctionDeclarationKind.BV_XOR -> {
                check(newArgs.size == 2 || newArgs.size == 3)
                check(functionDeclaration.argumentTypes.all { it.isBitvectorType })

                if (newArgs.size == 2)
                    newFormulaManager.bitvectorFormulaManager.xor(
                        newArgs[0] as BitvectorFormula,
                        newArgs[1] as BitvectorFormula
                    )
                else newFormulaManager.bitvectorFormulaManager.xor(
                    newArgs[0] as BitvectorFormula,
                    newFormulaManager.bitvectorFormulaManager.xor(
                        newArgs[1] as BitvectorFormula,
                        newArgs[2] as BitvectorFormula
                    )
                )
            }

            FunctionDeclarationKind.BV_NOT -> {
                check(newArgs.size == 1)
                check(functionDeclaration.argumentTypes.first().isBitvectorType)

                newFormulaManager.bitvectorFormulaManager.not(newArgs[0] as BitvectorFormula)
            }

            FunctionDeclarationKind.BV_LSHR -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isBitvectorType })
                newFormulaManager.bitvectorFormulaManager.shiftRight(newArgs[0] as BitvectorFormula, newArgs[1] as BitvectorFormula, false)
            }

            FunctionDeclarationKind.BV_SHL -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isBitvectorType })
                newFormulaManager.bitvectorFormulaManager.shiftLeft(newArgs[0] as BitvectorFormula, newArgs[1] as BitvectorFormula)
            }

            // Bitvector arithmetic

            FunctionDeclarationKind.BV_ADD -> {
                check(newArgs.size > 1)
                check(functionDeclaration.argumentTypes.all { it.isBitvectorType })
                newFormulaManager.bitvectorFormulaManager.add(newArgs as List<BitvectorFormula>)
            }

            FunctionDeclarationKind.BV_SUB -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isBitvectorType })
                newFormulaManager.bitvectorFormulaManager.subtract(newArgs[0] as BitvectorFormula, newArgs[1] as BitvectorFormula)
            }

            FunctionDeclarationKind.BV_MUL -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isBitvectorType })

                newFormulaManager.bitvectorFormulaManager.multiply(newArgs[0] as BitvectorFormula, newArgs[1] as BitvectorFormula)
            }

            FunctionDeclarationKind.BV_SDIV -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isBitvectorType })
                newFormulaManager.bitvectorFormulaManager.divide(newArgs[0] as BitvectorFormula, newArgs[1] as BitvectorFormula, true)
            }

            FunctionDeclarationKind.BV_UDIV -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isBitvectorType })
                newFormulaManager.bitvectorFormulaManager.divide(newArgs[0] as BitvectorFormula, newArgs[1] as BitvectorFormula, false)
            }

            FunctionDeclarationKind.BV_NEG -> {
                check(newArgs.size == 1)
                check(functionDeclaration.argumentTypes.all { it.isBitvectorType })
                newFormulaManager.bitvectorFormulaManager.negate(newArgs[0] as BitvectorFormula)
            }

            FunctionDeclarationKind.BV_ASHR -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isBitvectorType })
                newFormulaManager.bitvectorFormulaManager.shiftRight(newArgs[0] as BitvectorFormula, newArgs[1] as BitvectorFormula, true)
            }

            FunctionDeclarationKind.BV_SREM -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isBitvectorType })
                newFormulaManager.bitvectorFormulaManager.modulo(newArgs[0] as BitvectorFormula, newArgs[1] as BitvectorFormula, true)
            }

            FunctionDeclarationKind.BV_UREM -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isBitvectorType })
                newFormulaManager.bitvectorFormulaManager.modulo(newArgs[0] as BitvectorFormula, newArgs[1] as BitvectorFormula, false)
            }

            // Bitvector comparison

            FunctionDeclarationKind.BV_EQ -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isBitvectorType })
                newFormulaManager.bitvectorFormulaManager.equal(newArgs[0] as BitvectorFormula, newArgs[1] as BitvectorFormula)
            }

            FunctionDeclarationKind.BV_SGE -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isBitvectorType })
                newFormulaManager.bitvectorFormulaManager.greaterOrEquals(newArgs[0] as BitvectorFormula, newArgs[1] as BitvectorFormula, true)
            }

            FunctionDeclarationKind.BV_UGE -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isBitvectorType })
                newFormulaManager.bitvectorFormulaManager.greaterOrEquals(newArgs[0] as BitvectorFormula, newArgs[1] as BitvectorFormula, false)
            }

            FunctionDeclarationKind.BV_SGT -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isBitvectorType })
                newFormulaManager.bitvectorFormulaManager.greaterThan(newArgs[0] as BitvectorFormula, newArgs[1] as BitvectorFormula, true)
            }

            FunctionDeclarationKind.BV_UGT -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isBitvectorType })
                newFormulaManager.bitvectorFormulaManager.greaterThan(newArgs[0] as BitvectorFormula, newArgs[1] as BitvectorFormula, false)
            }

            FunctionDeclarationKind.BV_SLE -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isBitvectorType })
                newFormulaManager.bitvectorFormulaManager.lessOrEquals(newArgs[0] as BitvectorFormula, newArgs[1] as BitvectorFormula, true)
            }

            FunctionDeclarationKind.BV_ULE -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isBitvectorType })
                newFormulaManager.bitvectorFormulaManager.lessOrEquals(newArgs[0] as BitvectorFormula, newArgs[1] as BitvectorFormula, false)
            }

            FunctionDeclarationKind.BV_SLT -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isBitvectorType })
                newFormulaManager.bitvectorFormulaManager.lessThan(newArgs[0] as BitvectorFormula, newArgs[1] as BitvectorFormula, true)
            }

            FunctionDeclarationKind.BV_ULT -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isBitvectorType })
                newFormulaManager.bitvectorFormulaManager.lessThan(newArgs[0] as BitvectorFormula, newArgs[1] as BitvectorFormula, false)
            }

            // other bitvector shit

            FunctionDeclarationKind.BV_EXTRACT -> {
                check(newArgs.size == 1)
                check(functionDeclaration.argumentTypes.first().isBitvectorType)

                val higher = context.formulaManager.getIntParam(functionDeclaration, 0)
                val lower = context.formulaManager.getIntParam(functionDeclaration, 1)

                newFormulaManager.bitvectorFormulaManager.extract(newArgs[0] as BitvectorFormula, higher, lower)
            }

            FunctionDeclarationKind.BV_SIGN_EXTENSION -> {
                check(newArgs.size == 1)
                check(functionDeclaration.argumentTypes.first().isBitvectorType)

                val bit = context.formulaManager.getIntParam(functionDeclaration, 0)

                newFormulaManager.bitvectorFormulaManager.extend(newArgs[0] as BitvectorFormula, bit, true)
            }

            FunctionDeclarationKind.BV_ZERO_EXTENSION -> {
                check(newArgs.size == 1)
                check(functionDeclaration.argumentTypes.first().isBitvectorType)

                val bit = context.formulaManager.getIntParam(functionDeclaration, 0)

                newFormulaManager.bitvectorFormulaManager.extend(newArgs[0] as BitvectorFormula, bit, false)
            }

            FunctionDeclarationKind.BV_SCASTTO_FP -> {
                check(newArgs.size == 1)
                check(functionDeclaration.argumentTypes.all { it.isBitvectorType })
                val totalBits = newFormulaManager.bitvectorFormulaManager.getLength(newArgs[0] as BitvectorFormula)
                val fpType = when (totalBits) {
                    FloatingPointType.getSinglePrecisionFloatingPointType().totalSize -> FloatingPointType.getSinglePrecisionFloatingPointType()
                    FloatingPointType.getDoublePrecisionFloatingPointType().totalSize -> FormulaType.getDoublePrecisionFloatingPointType()
                    else -> error("unknown FP type from bitvector of size $totalBits")
                }

                newFormulaManager.floatingPointFormulaManager.castFrom(newArgs[0] as BitvectorFormula, true, fpType)
            }

            FunctionDeclarationKind.BV_UCASTTO_FP -> {
                check(newArgs.size == 1)
                check(functionDeclaration.argumentTypes.all { it.isBitvectorType })
                val totalBits = newFormulaManager.bitvectorFormulaManager.getLength(newArgs[0] as BitvectorFormula)
                val fpType = when (totalBits) {
                    FloatingPointType.getSinglePrecisionFloatingPointType().totalSize -> FloatingPointType.getSinglePrecisionFloatingPointType()
                    FloatingPointType.getDoublePrecisionFloatingPointType().totalSize -> FormulaType.getDoublePrecisionFloatingPointType()
                    else -> error("unknown FP type from bitvector of size $totalBits")
                }

                newFormulaManager.floatingPointFormulaManager.castFrom(newArgs[0] as BitvectorFormula, false, fpType)
            }


            FunctionDeclarationKind.BV_CONCAT -> {
                check(newArgs.size == 2)
                check(functionDeclaration.argumentTypes.all { it.isBitvectorType })

                newFormulaManager.bitvectorFormulaManager.concat(newArgs[0] as BitvectorFormula, newArgs[1] as BitvectorFormula)
            }


            else -> TODO("Yet not implemented; kind: ${functionDeclaration.kind} of formula: $formula (${formula::class})")
        }
    }
}