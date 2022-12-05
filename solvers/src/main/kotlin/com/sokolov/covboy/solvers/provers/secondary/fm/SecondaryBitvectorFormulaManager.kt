package com.sokolov.covboy.solvers.provers.secondary.fm

import org.sosy_lab.java_smt.api.*
import java.math.BigInteger

class SecondaryBitvectorFormulaManager(
    private val originalFm: BitvectorFormulaManager,

    private val delegate: BitvectorFormulaManager,

    secondaryFM: ISecondaryFM
) : BitvectorFormulaManager, ISecondaryFM by secondaryFM {

    override fun makeBitvector(length: Int, value: Long): BitvectorFormula {
        return mapper.toSecondary(originalFm.makeBitvector(length, value))
    }

    override fun makeBitvector(length: Int, value: BigInteger): BitvectorFormula {
        return mapper.toSecondary(originalFm.makeBitvector(length, value))
    }

    override fun makeBitvector(length: Int, value: NumeralFormula.IntegerFormula): BitvectorFormula {
        if (areAnySecondaryFormula(value)) {
            val originalValue = value.asOriginal()
            return makeBitvector(length, originalValue)
        }
        return mapper.toSecondary(originalFm.makeBitvector(length, value))
    }

    override fun toIntegerFormula(value: BitvectorFormula, signed: Boolean): NumeralFormula.IntegerFormula {
        if (areAnySecondaryFormula(value)) {
            val originalValue = value.asOriginal()
            return toIntegerFormula(originalValue, signed)
        }
        return mapper.toSecondary(originalFm.toIntegerFormula(value, signed))
    }

    override fun makeVariable(length: Int, value: String): BitvectorFormula {
        return mapper.toSecondary(originalFm.makeVariable(length, value))
    }

    override fun makeVariable(type: FormulaType.BitvectorType, value: String): BitvectorFormula {
        return mapper.toSecondary(originalFm.makeVariable(type, value))
    }

    override fun getLength(bv: BitvectorFormula): Int {
        if (areAnySecondaryFormula(bv)) {
            val originalBv = bv.asOriginal()

            return getLength(originalBv)
        }
        return originalFm.getLength(bv)
    }

    override fun negate(bv: BitvectorFormula): BitvectorFormula {
        if (areAnySecondaryFormula(bv)) {
            val originalBv = bv.asOriginal()
            return negate(originalBv)
        }
        return mapper.toSecondary(originalFm.negate(bv))
    }

    override fun add(bv1: BitvectorFormula, bv2: BitvectorFormula): BitvectorFormula {
        if (areAnySecondaryFormula(bv1, bv2)) {
            val originalBv1 = bv1.asOriginal()
            val originalBv2 = bv2.asOriginal()
            return add(originalBv1, originalBv2)
        }
        return mapper.toSecondary(originalFm.add(bv1, bv2))
    }

    override fun subtract(bv1: BitvectorFormula, bv2: BitvectorFormula): BitvectorFormula {
        if (areAnySecondaryFormula(bv1, bv2)) {
            val originalBv1 = bv1.asOriginal()
            val originalBv2 = bv2.asOriginal()
            return subtract(originalBv1, originalBv2)
        }
        return mapper.toSecondary(originalFm.subtract(bv1, bv2))
    }

    override fun divide(bv1: BitvectorFormula, bv2: BitvectorFormula, signed: Boolean): BitvectorFormula {
        if (areAnySecondaryFormula(bv1, bv2)) {
            val originalBv1 = bv1.asOriginal()
            val originalBv2 = bv2.asOriginal()
            return divide(originalBv1, originalBv2, signed)
        }
        return mapper.toSecondary(originalFm.divide(bv1, bv2, signed))
    }

    override fun modulo(bv1: BitvectorFormula, bv2: BitvectorFormula, signed: Boolean): BitvectorFormula {
        if (areAnySecondaryFormula(bv1, bv2)) {
            val originalBv1 = bv1.asOriginal()
            val originalBv2 = bv2.asOriginal()
            return modulo(originalBv1, originalBv2, signed)
        }
        return mapper.toSecondary(originalFm.modulo(bv1, bv2, signed))
    }

    override fun multiply(bv1: BitvectorFormula, bv2: BitvectorFormula): BitvectorFormula {
        if (areAnySecondaryFormula(bv1, bv2)) {
            val originalBv1 = bv1.asOriginal()
            val originalBv2 = bv2.asOriginal()
            return multiply(originalBv1, originalBv2)
        }
        return mapper.toSecondary(originalFm.multiply(bv1, bv2))
    }

    override fun equal(bv1: BitvectorFormula, bv2: BitvectorFormula): BooleanFormula {
        if (areAnySecondaryFormula(bv1, bv2)) {
            val originalBv1 = bv1.asOriginal()
            val originalBv2 = bv2.asOriginal()
            return equal(originalBv1, originalBv2)
        }
        return mapper.toSecondary(originalFm.equal(bv1, bv2))
    }

    override fun greaterThan(bv1: BitvectorFormula, bv2: BitvectorFormula, signed: Boolean): BooleanFormula {
        if (areAnySecondaryFormula(bv1, bv2)) {
            val originalBv1 = bv1.asOriginal()
            val originalBv2 = bv2.asOriginal()
            return greaterThan(originalBv1, originalBv2, signed)
        }
        return mapper.toSecondary(originalFm.greaterThan(bv1, bv2, signed))
    }

    override fun greaterOrEquals(bv1: BitvectorFormula, bv2: BitvectorFormula, signed: Boolean): BooleanFormula {
        if (areAnySecondaryFormula(bv1, bv2)) {
            val originalBv1 = bv1.asOriginal()
            val originalBv2 = bv2.asOriginal()
            return greaterOrEquals(originalBv1, originalBv2, signed)
        }
        return mapper.toSecondary(originalFm.greaterOrEquals(bv1, bv2, signed))
    }

    override fun lessThan(bv1: BitvectorFormula, bv2: BitvectorFormula, signed: Boolean): BooleanFormula {
        if (areAnySecondaryFormula(bv1, bv2)) {
            val originalBv1 = bv1.asOriginal()
            val originalBv2 = bv2.asOriginal()
            return lessThan(originalBv1, originalBv2, signed)
        }
        return mapper.toSecondary(originalFm.lessThan(bv1, bv2, signed))
    }

    override fun lessOrEquals(bv1: BitvectorFormula, bv2: BitvectorFormula, signed: Boolean): BooleanFormula {
        if (areAnySecondaryFormula(bv1, bv2)) {
            val originalBv1 = bv1.asOriginal()
            val originalBv2 = bv2.asOriginal()
            return lessOrEquals(originalBv1, originalBv2, signed)
        }
        return mapper.toSecondary(originalFm.lessOrEquals(bv1, bv2, signed))
    }

    override fun not(bv: BitvectorFormula): BitvectorFormula {
        if (areAnySecondaryFormula(bv)) {
            val originalBv = bv.asOriginal()
            return not(originalBv)
        }
        return mapper.toSecondary(originalFm.not(bv))
    }

    override fun and(bv1: BitvectorFormula, bv2: BitvectorFormula): BitvectorFormula {
        if (areAnySecondaryFormula(bv1, bv2)) {
            val originalBv1 = bv1.asOriginal()
            val originalBv2 = bv2.asOriginal()
            return and(originalBv1, originalBv2)
        }
        return mapper.toSecondary(originalFm.and(bv1, bv2))
    }

    override fun or(bv1: BitvectorFormula, bv2: BitvectorFormula): BitvectorFormula {
        if (areAnySecondaryFormula(bv1, bv2)) {
            val originalBv1 = bv1.asOriginal()
            val originalBv2 = bv2.asOriginal()
            return or(originalBv1, originalBv2)
        }
        return mapper.toSecondary(originalFm.or(bv1, bv2))
    }

    override fun xor(bv1: BitvectorFormula, bv2: BitvectorFormula): BitvectorFormula {
        if (areAnySecondaryFormula(bv1, bv2)) {
            val originalBv1 = bv1.asOriginal()
            val originalBv2 = bv2.asOriginal()
            return xor(originalBv1, originalBv2)
        }
        return mapper.toSecondary(originalFm.xor(bv1, bv2))
    }

    override fun shiftRight(number: BitvectorFormula, toShift: BitvectorFormula, signed: Boolean): BitvectorFormula {
        if (areAnySecondaryFormula(number, toShift)) {
            val originalNumber = number.asOriginal()
            val originalToShift = toShift.asOriginal()
            return shiftRight(originalNumber, originalToShift, signed)
        }
        return mapper.toSecondary(originalFm.shiftRight(number, toShift, signed))
    }

    override fun shiftLeft(number: BitvectorFormula, toShift: BitvectorFormula): BitvectorFormula {
        if (areAnySecondaryFormula(number, toShift)) {
            val originalNumber = number.asOriginal()
            val originalToShift = toShift.asOriginal()
            return shiftLeft(originalNumber, originalToShift)
        }
        return mapper.toSecondary(originalFm.shiftLeft(number, toShift))
    }

    override fun concat(number: BitvectorFormula, append: BitvectorFormula): BitvectorFormula {
        if (areAnySecondaryFormula(number, append)) {
            val originalNumber = number.asOriginal()
            val originalAppend = append.asOriginal()
            return concat(originalNumber, originalAppend)
        }
        return mapper.toSecondary(originalFm.concat(number, append))
    }

    override fun extract(bv: BitvectorFormula, msb: Int, lsb: Int): BitvectorFormula {
        if (areAnySecondaryFormula(bv)) {
            val originalBv = bv.asOriginal()
            return extract(originalBv, msb, lsb)
        }
        return mapper.toSecondary(originalFm.extract(bv, msb, lsb))
    }

    override fun extend(bv: BitvectorFormula, extensionBit: Int, signed: Boolean): BitvectorFormula {
        if (areAnySecondaryFormula(bv)) {
            val originalBv = bv.asOriginal()
            return extend(originalBv, extensionBit, signed)
        }
        return mapper.toSecondary(originalFm.extend(bv, extensionBit, signed))
    }

    override fun distinct(bvs: List<BitvectorFormula>): BooleanFormula {
        if (areAnySecondaryFormula(*bvs.toTypedArray())) {
            val originalBvs = bvs.map { it.asOriginal() }
            return distinct(originalBvs)
        }
        return mapper.toSecondary(originalFm.distinct(bvs))
    }
}