package com.sokolov.covboy.prover.secondary

import com.sokolov.covboy.smt.isFormulaSupported
import com.sokolov.covboy.smt.isNotVisit
import com.sokolov.covboy.smt.notOptimized
import org.sosy_lab.java_smt.SolverContextFactory
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.BooleanFormulaManager
import org.sosy_lab.java_smt.api.Formula
import org.sosy_lab.java_smt.api.visitors.BooleanFormulaTransformationVisitor
import org.sosy_lab.java_smt.api.visitors.BooleanFormulaVisitor
import org.sosy_lab.java_smt.api.visitors.TraversalProcess

class SecondaryBooleanFormulaManager(
    private val originalFm: BooleanFormulaManager,
    private val delegate: BooleanFormulaManager,
    private val secondarySolver: SolverContextFactory.Solvers,

    private val mapper: FormulaMapper
) : BooleanFormulaManager by delegate {

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
        if (areSecondaryFormulas(first, second)) {
            val originalFirst = mapper.findOriginal(first) ?: error("not found original term $first")
            val originalSecond = mapper.findOriginal(second) ?: error("not found original term $second")
            return equivalence(originalFirst, originalSecond)
        }

        return mapper.toSecondary(originalFm.equivalence(first, second))
    }

    override fun implication(from: BooleanFormula, to: BooleanFormula): BooleanFormula {
        if (areSecondaryFormulas(from, to)) {
            val originalFrom = mapper.findOriginal(from) ?: error("not found original term $from")
            val originalTo = mapper.findOriginal(to) ?: error("not found original term $to")
            return implication(originalFrom, originalTo)
        }
        return mapper.toSecondary(originalFm.implication(from, to))
    }

    override fun <T : Formula> ifThenElse(ifFormula: BooleanFormula, thenFormula: T, elseFormula: T): T {
        if (areSecondaryFormulas(ifFormula, thenFormula, elseFormula)) {
            val originalIf = mapper.findOriginal(ifFormula) ?: error("not found original term $ifFormula")
            val originalThen = mapper.findOriginal(thenFormula) ?: error("not found original term $thenFormula")
            val originalElse = mapper.findOriginal(elseFormula) ?: error("not found original term $elseFormula")
            return ifThenElse(originalIf, originalThen, originalElse)
        }

        return mapper.toSecondary(originalFm.ifThenElse(ifFormula, thenFormula, elseFormula))
    }

    override fun not(f: BooleanFormula): BooleanFormula {
        if (areSecondaryFormulas(f)) {
            val originalF = mapper.findOriginal(f) ?: error("not found original term $f")
            return not(originalF)
        }

//        return mapper.toSecondary(originalFm.not(f))
        return mapper.toSecondary(originalFm.notOptimized(f))
    }

    override fun and(first: BooleanFormula, second: BooleanFormula): BooleanFormula {
        if (areSecondaryFormulas(first, second)) {
            val originalFirst = mapper.findOriginal(first) ?: error("not found original term $first")
            val originalSecond = mapper.findOriginal(second) ?: error("not found original term $second")
            return and(originalFirst, originalSecond)
        }

        return mapper.toSecondary(originalFm.and(first, second))
    }

    override fun and(formulas: Collection<BooleanFormula>): BooleanFormula {
        if (areSecondaryFormulas(*formulas.toTypedArray())) {
            val originals = formulas.map { mapper.findOriginal(it) ?: error("not found original term $it") }
            return and(originals)
        }
        return mapper.toSecondary(originalFm.and(formulas))
    }

    override fun and(vararg formulas: BooleanFormula): BooleanFormula {
        if (areSecondaryFormulas(*formulas)) {
            val originals = formulas.map { mapper.findOriginal(it) ?: error("not found original term $it") }
            return and(*originals.toTypedArray())
        }

        return mapper.toSecondary(originalFm.and(*formulas))
    }


    override fun or(first: BooleanFormula, second: BooleanFormula): BooleanFormula {
        if (areSecondaryFormulas(first, second)) {
            val originalFirst = mapper.findOriginal(first) ?: error("not found original term $first")
            val originalSecond = mapper.findOriginal(second) ?: error("not found original term $second")
            return or(originalFirst, originalSecond)
        }

        return mapper.toSecondary(originalFm.or(first, second))
    }

    override fun or(formulas: Collection<BooleanFormula>): BooleanFormula {
        if (areSecondaryFormulas(*formulas.toTypedArray())) {
            val originals = formulas.map { mapper.findOriginal(it) ?: error("not found original term $it") }
            return or(originals)
        }

        return mapper.toSecondary(originalFm.or(formulas))
    }

    override fun or(vararg formulas: BooleanFormula): BooleanFormula {
        if (areSecondaryFormulas(*formulas)) {
            val originals = formulas.map { mapper.findOriginal(it) ?: error("not found original term $it") }
            return or(*originals.toTypedArray())
        }

        return mapper.toSecondary(originalFm.or(*formulas))
    }


    override fun xor(first: BooleanFormula, second: BooleanFormula): BooleanFormula {
        if (areSecondaryFormulas(first, second)) {
            val originalFirst = mapper.findOriginal(first) ?: error("not found original term $first")
            val originalSecond = mapper.findOriginal(second) ?: error("not found original term $second")
            return xor(originalFirst, originalSecond)
        }

        return mapper.toSecondary(originalFm.xor(first, second))
    }

    override fun <R : Any> visit(f: BooleanFormula, visitor: BooleanFormulaVisitor<R>): R {
        if (areSecondaryFormulas(f)) {
            val originalF = mapper.findOriginal(f) ?: error("not found original term $f")
            return visit(originalF, visitor)
        }
        return originalFm.visit(f, visitor)
    }

    override fun visitRecursively(f: BooleanFormula, visitor: BooleanFormulaVisitor<TraversalProcess>) {
        if (areSecondaryFormulas(f)) {
            val originalF = mapper.findOriginal(f) ?: error("not found original term $f")
            return visitRecursively(originalF, visitor)
        }

        originalFm.visitRecursively(f, visitor)
    }

    override fun transformRecursively(f: BooleanFormula, visitor: BooleanFormulaTransformationVisitor): BooleanFormula {
        if (areSecondaryFormulas(f)) {
            val originalF = mapper.findOriginal(f) ?: error("not found original term $f")
            return transformRecursively(originalF, visitor)
        }

        return originalFm.transformRecursively(f, visitor)
    }

    override fun toConjunctionArgs(f: BooleanFormula, p1: Boolean): Set<BooleanFormula> {
        if (areSecondaryFormulas(f)) {
            val originalF = mapper.findOriginal(f) ?: error("not found original term $f")
            return toConjunctionArgs(originalF, p1)
        }
        return originalFm.toConjunctionArgs(f, p1)
    }

    override fun toDisjunctionArgs(f: BooleanFormula, p1: Boolean): Set<BooleanFormula> {
        if (areSecondaryFormulas(f)) {
            val originalF = mapper.findOriginal(f) ?: error("not found original term $f")
            return toDisjunctionArgs(originalF, p1)
        }

        return originalFm.toDisjunctionArgs(f, p1)
    }

    private fun areSecondaryFormulas(vararg formulas: Formula) = formulas.all { secondarySolver.isFormulaSupported(it) }

    fun isNot(formula: BooleanFormula): Boolean {
        if (areSecondaryFormulas(formula)) {
            val original = mapper.findOriginal(formula) ?: error("not found original term $formula")
            return isNot(original)
        }

        return originalFm.isNotVisit(formula)
    }
}