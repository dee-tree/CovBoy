package com.sokolov.covboy.solvers.provers.secondary.fm

import com.sokolov.covboy.solvers.formulas.utils.isNotVisit
import com.sokolov.covboy.solvers.formulas.utils.notOptimized
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.BooleanFormulaManager
import org.sosy_lab.java_smt.api.Formula
import org.sosy_lab.java_smt.api.visitors.BooleanFormulaTransformationVisitor
import org.sosy_lab.java_smt.api.visitors.BooleanFormulaVisitor
import org.sosy_lab.java_smt.api.visitors.TraversalProcess
import java.util.stream.Collector
import java.util.stream.Collectors

class SecondaryBooleanFormulaManager(
    private val originalFm: BooleanFormulaManager,
    private val delegate: BooleanFormulaManager,

    secondaryFM: ISecondaryFM
) : BooleanFormulaManager, ISecondaryFM by secondaryFM {


    override fun isTrue(bool: BooleanFormula): Boolean {
        if (areAnySecondaryFormula(bool)) {
            val originalBool = bool.asOriginal()
            return isTrue(originalBool)
        }

        return originalFm.isTrue(bool)
    }

    override fun isFalse(bool: BooleanFormula): Boolean {
        if (areAnySecondaryFormula(bool)) {
            val originalBool = bool.asOriginal()
            return isFalse(originalBool)
        }

        return originalFm.isFalse(bool)
    }

    override fun makeTrue(): BooleanFormula {
        return mapper.toSecondary(originalFm.makeTrue())
    }

    override fun makeFalse(): BooleanFormula {
        return mapper.toSecondary(originalFm.makeFalse())
    }

    override fun makeVariable(name: String): BooleanFormula {
        return mapper.toSecondary(originalFm.makeVariable(name))
    }

    override fun equivalence(first: BooleanFormula, second: BooleanFormula): BooleanFormula {
        if (areAnySecondaryFormula(first, second)) {
            val originalFirst = first.asOriginal()
            val originalSecond = second.asOriginal()
            return equivalence(originalFirst, originalSecond)
        }

        return mapper.toSecondary(originalFm.equivalence(first, second))
    }

    override fun implication(from: BooleanFormula, to: BooleanFormula): BooleanFormula {
        if (areAnySecondaryFormula(from, to)) {
            val originalFrom = from.asOriginal()
            val originalTo = to.asOriginal()
            return implication(originalFrom, originalTo)
        }
        return mapper.toSecondary(originalFm.implication(from, to))
    }

    override fun <T : Formula> ifThenElse(ifFormula: BooleanFormula, thenFormula: T, elseFormula: T): T {
        if (areAnySecondaryFormula(ifFormula, thenFormula, elseFormula)) {
            val originalIf = ifFormula.asOriginal()
            val originalThen = thenFormula.asOriginal()
            val originalElse = elseFormula.asOriginal()
            return ifThenElse(originalIf, originalThen, originalElse)
        }

        return mapper.toSecondary(originalFm.ifThenElse(ifFormula, thenFormula, elseFormula))
    }

    override fun not(f: BooleanFormula): BooleanFormula {
        if (areAnySecondaryFormula(f)) {
            val originalF = f.asOriginal()
            return not(originalF)
        }

//        return mapper.toSecondary(originalFm.not(f))
        return mapper.toSecondary(originalFm.notOptimized(f))
    }

    override fun and(first: BooleanFormula, second: BooleanFormula): BooleanFormula {
        if (areAnySecondaryFormula(first, second)) {
            val originalFirst = first.asOriginal()
            val originalSecond = second.asOriginal()
            return and(originalFirst, originalSecond)
        }

        return mapper.toSecondary(originalFm.and(first, second))
    }

    override fun and(formulas: Collection<BooleanFormula>): BooleanFormula {
        if (areAnySecondaryFormula(*formulas.toTypedArray())) {
            val originals = formulas.map { it.asOriginal() }
            return and(originals)
        }
        return mapper.toSecondary(originalFm.and(formulas))
    }

    override fun and(vararg formulas: BooleanFormula): BooleanFormula {
        if (areAnySecondaryFormula(*formulas)) {
            val originals = formulas.map { it.asOriginal() }
            return and(*originals.toTypedArray())
        }

        return mapper.toSecondary(originalFm.and(*formulas))
    }


    override fun or(first: BooleanFormula, second: BooleanFormula): BooleanFormula {
        if (areAnySecondaryFormula(first, second)) {
            val originalFirst = first.asOriginal()
            val originalSecond = second.asOriginal()
            return or(originalFirst, originalSecond)
        }

        return mapper.toSecondary(originalFm.or(first, second))
    }

    override fun or(formulas: Collection<BooleanFormula>): BooleanFormula {
        if (areAnySecondaryFormula(*formulas.toTypedArray())) {
            val originals = formulas.map { it.asOriginal() }
            return or(originals)
        }

        return mapper.toSecondary(originalFm.or(formulas))
    }

    override fun or(vararg formulas: BooleanFormula): BooleanFormula {
        if (areAnySecondaryFormula(*formulas)) {

            val originals = formulas.map { it.asOriginal() }
            return or(*originals.toTypedArray())
        }

        return mapper.toSecondary(originalFm.or(*formulas))
    }


    override fun xor(first: BooleanFormula, second: BooleanFormula): BooleanFormula {
        if (areAnySecondaryFormula(first, second)) {
            val originalFirst = first.asOriginal()
            val originalSecond = second.asOriginal()
            return xor(originalFirst, originalSecond)
        }

        return mapper.toSecondary(originalFm.xor(first, second))
    }

    override fun <R : Any> visit(f: BooleanFormula, visitor: BooleanFormulaVisitor<R>): R {
        if (areAnySecondaryFormula(f)) {
            val originalF = f.asOriginal()
            return visit(originalF, visitor)
        }

        val res = originalFm.visit(f, visitor)
        return if (res is Formula && !areSecondaryFormulas(res)) mapper.toSecondary(res) else res
    }

    override fun visitRecursively(f: BooleanFormula, visitor: BooleanFormulaVisitor<TraversalProcess>) {
        if (areAnySecondaryFormula(f)) {
            val originalF = f.asOriginal()
            return visitRecursively(originalF, visitor)
        }

        originalFm.visitRecursively(f, visitor)
    }

    override fun transformRecursively(f: BooleanFormula, visitor: BooleanFormulaTransformationVisitor): BooleanFormula {
        if (areAnySecondaryFormula(f)) {
            val originalF = f.asOriginal()
            return transformRecursively(originalF, visitor)
        }

        return originalFm.transformRecursively(f, visitor)
    }

    override fun toConjunctionArgs(f: BooleanFormula, p1: Boolean): Set<BooleanFormula> {
        if (areAnySecondaryFormula(f)) {
            val originalF = f.asOriginal()
            return toConjunctionArgs(originalF, p1)
        }
        return originalFm.toConjunctionArgs(f, p1)
    }

    override fun toDisjunctionArgs(f: BooleanFormula, p1: Boolean): Set<BooleanFormula> {
        if (areAnySecondaryFormula(f)) {
            val originalF = f.asOriginal()
            return toDisjunctionArgs(originalF, p1)
        }

        return originalFm.toDisjunctionArgs(f, p1)
    }

    override fun toConjunction(): Collector<BooleanFormula, *, BooleanFormula> {
        return Collectors.collectingAndThen(Collectors.toList()) { formulas: Collection<BooleanFormula> ->
            and(formulas.map { it.asOriginal() })
        }
    }

    override fun toDisjunction(): Collector<BooleanFormula, *, BooleanFormula> {
        return Collectors.collectingAndThen(Collectors.toList()) { formulas: Collection<BooleanFormula> ->
            or(formulas.map { it.asOriginal() })
        }
    }

    fun isNot(formula: BooleanFormula): Boolean {
        if (areAnySecondaryFormula(formula)) {
            val original = formula.asOriginal()
            return isNot(original)
        }

        return originalFm.isNotVisit(formula)
    }
}