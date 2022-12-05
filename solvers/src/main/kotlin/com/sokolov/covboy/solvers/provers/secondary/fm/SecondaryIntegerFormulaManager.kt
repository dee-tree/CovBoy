package com.sokolov.covboy.solvers.provers.secondary.fm

import org.sosy_lab.common.rationals.Rational
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.IntegerFormulaManager
import org.sosy_lab.java_smt.api.NumeralFormula
import java.math.BigDecimal
import java.math.BigInteger

class SecondaryIntegerFormulaManager(
    private val originalFm: IntegerFormulaManager,
    private val delegate: IntegerFormulaManager,

    secondaryFM: ISecondaryFM
) : IntegerFormulaManager, ISecondaryFM by secondaryFM {

    override fun makeNumber(value: Long): NumeralFormula.IntegerFormula {
        return mapper.toSecondary(originalFm.makeNumber(value))
    }

    override fun makeNumber(value: BigInteger): NumeralFormula.IntegerFormula {
        return mapper.toSecondary(originalFm.makeNumber(value))
    }

    override fun makeNumber(value: Double): NumeralFormula.IntegerFormula {
        return mapper.toSecondary(originalFm.makeNumber(value))
    }

    override fun makeNumber(value: BigDecimal): NumeralFormula.IntegerFormula {
        return mapper.toSecondary(originalFm.makeNumber(value))
    }

    override fun makeNumber(value: String): NumeralFormula.IntegerFormula {
        return mapper.toSecondary(originalFm.makeNumber(value))
    }

    override fun makeNumber(value: Rational): NumeralFormula.IntegerFormula {
        return mapper.toSecondary(originalFm.makeNumber(value))
    }

    override fun makeVariable(value: String): NumeralFormula.IntegerFormula {
        return mapper.toSecondary(originalFm.makeNumber(value))
    }

    override fun negate(f: NumeralFormula.IntegerFormula): NumeralFormula.IntegerFormula {
        if (areAnySecondaryFormula(f)) {
            val originalF = f.asOriginal()
            return negate(originalF)
        }
        return mapper.toSecondary(originalFm.negate(f))
    }

    override fun add(
        first: NumeralFormula.IntegerFormula,
        second: NumeralFormula.IntegerFormula
    ): NumeralFormula.IntegerFormula {
        if (areAnySecondaryFormula(first, second)) {
            val originalFirst = first.asOriginal()
            val originalSecond = second.asOriginal()
            return add(originalFirst, originalSecond)
        }
        return mapper.toSecondary(originalFm.add(first, second))
    }

    override fun sum(formulas: List<NumeralFormula.IntegerFormula>): NumeralFormula.IntegerFormula {
        if (areAnySecondaryFormula(*formulas.toTypedArray())) {
            val originalFormulas = formulas.map { it.asOriginal() }
            return sum(originalFormulas)
        }
        return mapper.toSecondary(originalFm.sum(formulas))
    }

    override fun subtract(
        reduced: NumeralFormula.IntegerFormula,
        subtracted: NumeralFormula.IntegerFormula
    ): NumeralFormula.IntegerFormula {
        if (areAnySecondaryFormula(reduced, subtracted)) {
            val originalReduced = reduced.asOriginal()
            val originalSubtracted = subtracted.asOriginal()
            return subtract(originalReduced, originalSubtracted)
        }
        return mapper.toSecondary(originalFm.subtract(reduced, subtracted))
    }

    override fun divide(
        diviseble: NumeralFormula.IntegerFormula,
        divider: NumeralFormula.IntegerFormula
    ): NumeralFormula.IntegerFormula {
        if (areAnySecondaryFormula(diviseble, divider)) {
            val originalDiviseble = diviseble.asOriginal()
            val originalDivider = divider.asOriginal()
            return divide(originalDiviseble, originalDivider)
        }
        return mapper.toSecondary(originalFm.divide(diviseble, divider))
    }

    override fun multiply(
        first: NumeralFormula.IntegerFormula,
        second: NumeralFormula.IntegerFormula
    ): NumeralFormula.IntegerFormula {
        if (areAnySecondaryFormula(first, second)) {
            val originalFirst = first.asOriginal()
            val originalSecond = second.asOriginal()
            return multiply(originalFirst, originalSecond)
        }
        return mapper.toSecondary(originalFm.multiply(first, second))
    }

    override fun equal(first: NumeralFormula.IntegerFormula, second: NumeralFormula.IntegerFormula): BooleanFormula {
        if (areAnySecondaryFormula(first, second)) {
            val originalFirst = first.asOriginal()
            val originalSecond = second.asOriginal()
            return equal(originalFirst, originalSecond)
        }
        return mapper.toSecondary(originalFm.equal(first, second))
    }

    override fun distinct(formulas: List<NumeralFormula.IntegerFormula>): BooleanFormula {
        if (areAnySecondaryFormula(*formulas.toTypedArray())) {
            val originalFormulas = formulas.map { it.asOriginal() }
            return distinct(originalFormulas)
        }
        return mapper.toSecondary(originalFm.distinct(formulas))
    }

    override fun greaterThan(
        first: NumeralFormula.IntegerFormula,
        second: NumeralFormula.IntegerFormula
    ): BooleanFormula {
        if (areAnySecondaryFormula(first, second)) {
            val originalFirst = first.asOriginal()
            val originalSecond = second.asOriginal()
            return greaterThan(originalFirst, originalSecond)
        }
        return mapper.toSecondary(originalFm.greaterThan(first, second))
    }

    override fun greaterOrEquals(
        first: NumeralFormula.IntegerFormula,
        second: NumeralFormula.IntegerFormula
    ): BooleanFormula {
        if (areAnySecondaryFormula(first, second)) {
            val originalFirst = first.asOriginal()
            val originalSecond = second.asOriginal()
            return greaterOrEquals(originalFirst, originalSecond)
        }
        return mapper.toSecondary(originalFm.greaterOrEquals(first, second))
    }

    override fun lessThan(first: NumeralFormula.IntegerFormula, second: NumeralFormula.IntegerFormula): BooleanFormula {
        if (areAnySecondaryFormula(first, second)) {
            val originalFirst = first.asOriginal()
            val originalSecond = second.asOriginal()
            return lessThan(originalFirst, originalSecond)
        }
        return mapper.toSecondary(originalFm.lessThan(first, second))
    }

    override fun lessOrEquals(
        first: NumeralFormula.IntegerFormula,
        second: NumeralFormula.IntegerFormula
    ): BooleanFormula {
        if (areAnySecondaryFormula(first, second)) {
            val originalFirst = first.asOriginal()
            val originalSecond = second.asOriginal()
            return lessOrEquals(originalFirst, originalSecond)
        }
        return mapper.toSecondary(originalFm.lessOrEquals(first, second))
    }

    override fun floor(f: NumeralFormula.IntegerFormula): NumeralFormula.IntegerFormula {
        if (areAnySecondaryFormula(f)) {
            val originalF = f.asOriginal()
            return floor(originalF)
        }
        return mapper.toSecondary(originalFm.floor(f))
    }

    override fun modularCongruence(
        p0: NumeralFormula.IntegerFormula,
        p1: NumeralFormula.IntegerFormula,
        p2: BigInteger
    ): BooleanFormula {
        if (areAnySecondaryFormula(p0, p1)) {
            val originalP0 = p0.asOriginal()
            val originalP1 = p1.asOriginal()
            return modularCongruence(originalP0, originalP1, p2)
        }
        return mapper.toSecondary(originalFm.modularCongruence(p0, p1, p2))
    }

    override fun modularCongruence(
        p0: NumeralFormula.IntegerFormula,
        p1: NumeralFormula.IntegerFormula,
        p2: Long
    ): BooleanFormula {
        if (areAnySecondaryFormula(p0, p1)) {
            val originalP0 = p0.asOriginal()
            val originalP1 = p1.asOriginal()
            return modularCongruence(originalP0, originalP1, p2)
        }
        return mapper.toSecondary(originalFm.modularCongruence(p0, p1, p2))
    }

    override fun modulo(
        first: NumeralFormula.IntegerFormula,
        second: NumeralFormula.IntegerFormula
    ): NumeralFormula.IntegerFormula {
        if (areAnySecondaryFormula(first, second)) {
            val originalFirst = first.asOriginal()
            val originalSecond = second.asOriginal()
            return modulo(originalFirst, originalSecond)
        }
        return mapper.toSecondary(originalFm.modulo(first, second))
    }
}