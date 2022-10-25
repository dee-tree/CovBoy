package com.sokolov.covboy.prover.secondary

import org.sosy_lab.common.rationals.Rational
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.NumeralFormula
import org.sosy_lab.java_smt.api.RationalFormulaManager
import java.math.BigDecimal
import java.math.BigInteger

class SecondaryRationalFormulaManager(
    private val originalFm: RationalFormulaManager,
    private val delegate: RationalFormulaManager,

    secondaryFM: ISecondaryFM
) : RationalFormulaManager, ISecondaryFM by secondaryFM {

    override fun makeNumber(value: Long): NumeralFormula.RationalFormula {
        return mapper.toSecondary(originalFm.makeNumber(value))
    }

    override fun makeNumber(value: BigInteger): NumeralFormula.RationalFormula {
        return mapper.toSecondary(originalFm.makeNumber(value))
    }

    override fun makeNumber(value: Double): NumeralFormula.RationalFormula {
        return mapper.toSecondary(originalFm.makeNumber(value))
    }

    override fun makeNumber(value: BigDecimal): NumeralFormula.RationalFormula {
        return mapper.toSecondary(originalFm.makeNumber(value))
    }

    override fun makeNumber(value: String): NumeralFormula.RationalFormula {
        return mapper.toSecondary(originalFm.makeNumber(value))
    }

    override fun makeNumber(value: Rational): NumeralFormula.RationalFormula {
        return mapper.toSecondary(originalFm.makeNumber(value))
    }

    override fun makeVariable(value: String): NumeralFormula.RationalFormula {
        return mapper.toSecondary(originalFm.makeVariable(value))
    }

    override fun negate(f: NumeralFormula): NumeralFormula.RationalFormula {
        if (areAnySecondaryFormula(f)) {
            val originalF = f.asOriginal()
            return negate(originalF)
        }
        return mapper.toSecondary(originalFm.negate(f))
    }

    override fun add(first: NumeralFormula, second: NumeralFormula): NumeralFormula.RationalFormula {
        if (areAnySecondaryFormula(first, second)) {
            val originalFirst = first.asOriginal()
            val originalSecond = first.asOriginal()
            return add(originalFirst, originalSecond)
        }
        return mapper.toSecondary(originalFm.add(first, second))
    }

    override fun sum(formulas: List<NumeralFormula>): NumeralFormula.RationalFormula {
        if (areAnySecondaryFormula(*formulas.toTypedArray())) {
            val originalF = formulas.map { it.asOriginal() }
            return sum(originalF)
        }
        return mapper.toSecondary(originalFm.sum(formulas))
    }

    override fun subtract(reduced: NumeralFormula, subtracted: NumeralFormula): NumeralFormula.RationalFormula {
        if (areAnySecondaryFormula(reduced, subtracted)) {
            val originalReduced = reduced.asOriginal()
            val originalSubtracted = subtracted.asOriginal()
            return subtract(originalReduced, originalSubtracted)
        }
        return mapper.toSecondary(originalFm.subtract(reduced, subtracted))
    }

    override fun divide(diviseble: NumeralFormula, divider: NumeralFormula): NumeralFormula.RationalFormula {
        if (areAnySecondaryFormula(diviseble, divider)) {
            val originalDiviseble = diviseble.asOriginal()
            val originalDivider = divider.asOriginal()
            return divide(originalDiviseble, originalDivider)
        }
        return mapper.toSecondary(originalFm.divide(diviseble, divider))
    }

    override fun multiply(first: NumeralFormula, second: NumeralFormula): NumeralFormula.RationalFormula {
        if (areAnySecondaryFormula(first, second)) {
            val originalFirst = first.asOriginal()
            val originalSecond = second.asOriginal()
            return multiply(originalFirst, originalSecond)
        }
        return mapper.toSecondary(originalFm.multiply(first, second))
    }

    override fun equal(first: NumeralFormula, second: NumeralFormula): BooleanFormula {
        if (areAnySecondaryFormula(first, second)) {
            val originalFirst = first.asOriginal()
            val originalSecond = second.asOriginal()
            return equal(originalFirst, originalSecond)
        }
        return mapper.toSecondary(originalFm.equal(first, second))
    }

    override fun distinct(formulas: List<NumeralFormula>): BooleanFormula {
        if (areAnySecondaryFormula(*formulas.toTypedArray())) {
            val originalFormulas = formulas.map { it.asOriginal() }
            return distinct(originalFormulas)
        }
        return mapper.toSecondary(originalFm.distinct(formulas))
    }

    override fun greaterThan(first: NumeralFormula, second: NumeralFormula): BooleanFormula {
        if (areAnySecondaryFormula(first, second)) {
            val originalFirst = first.asOriginal()
            val originalSecond = second.asOriginal()
            return greaterThan(originalFirst, originalSecond)
        }
        return mapper.toSecondary(originalFm.greaterThan(first, second))
    }

    override fun greaterOrEquals(first: NumeralFormula, second: NumeralFormula): BooleanFormula {
        if (areAnySecondaryFormula(first, second)) {
            val originalFirst = first.asOriginal()
            val originalSecond = second.asOriginal()
            return greaterOrEquals(originalFirst, originalSecond)
        }
        return mapper.toSecondary(originalFm.greaterOrEquals(first, second))
    }

    override fun lessThan(first: NumeralFormula, second: NumeralFormula): BooleanFormula {
        if (areAnySecondaryFormula(first, second)) {
            val originalFirst = first.asOriginal()
            val originalSecond = second.asOriginal()
            return lessThan(originalFirst, originalSecond)
        }
        return mapper.toSecondary(originalFm.lessThan(first, second))
    }

    override fun lessOrEquals(first: NumeralFormula, second: NumeralFormula): BooleanFormula {
        if (areAnySecondaryFormula(first, second)) {
            val originalFirst = first.asOriginal()
            val originalSecond = second.asOriginal()
            return lessOrEquals(originalFirst, originalSecond)
        }
        return mapper.toSecondary(originalFm.lessOrEquals(first, second))
    }

    override fun floor(f: NumeralFormula): NumeralFormula.IntegerFormula {
        if (areAnySecondaryFormula(f)) {
            val originalF = f.asOriginal()
            return floor(originalF)
        }
        return mapper.toSecondary(originalFm.floor(f))
    }
}