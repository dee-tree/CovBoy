package com.sokolov.covboy.solvers.provers.secondary

import com.sokolov.covboy.solvers.formulas.*
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.Formula
import org.sosy_lab.java_smt.api.FormulaManager
import org.sosy_lab.java_smt.api.SolverContext
import org.sosy_lab.java_smt.solvers.z3.isZ3Formula
import org.sosy_lab.java_smt.solvers.z3.z3FormulaTransform

/**
 * mapper from original prover to secondary prover
 */
class FormulaMapper (
    private val originalContext: SolverContext,
    private val originalFm: FormulaManager,

    private val secondaryContext: SolverContext,
    private val secondaryFm: FormulaManager
    ) {

    /*
    Original to secondary formula mapper
     */
    private val storage = mutableMapOf<Formula, Formula>()

    fun clear() {
        storage.clear()
    }

    @Suppress("unchecked_cast")
    fun <T : Formula> toSecondary(original: T): T {
        return (storage[original] ?: original.transform()) as T
    }

    fun toSecondary(constraint: Constraint): Constraint = if (constraint is NonSwitchableConstraint) {
        val fSecondary = toSecondary(constraint.asFormula)
        fSecondary.asNonSwitchableConstraint()
    } else {
        check(constraint is SwitchableConstraint)
        val fSecondary = toSecondary(constraint.original)
        MutableSwitchableConstraint(fSecondary, fm = secondaryFm).also {
            storage[constraint.assumption] = it.assumption
        }
    }


    @Suppress("unchecked_cast")
    fun <T: Formula> findOriginal(secondary: T): T? = storage.entries.associate { it.value to it.key }[secondary] as? T


    fun findOriginal(secondary: Constraint): Constraint? = if (secondary is NonSwitchableConstraint) {
        findOriginal(secondary.asFormula)?.asNonSwitchableConstraint()
    } else {
        check(secondary is SwitchableConstraint)
        findOriginal(secondary.original)?.let { MutableSwitchableConstraint(it, fm = originalFm) }
    }


    /**
     * original to secondary formula transformation
     */
    private fun <T : Formula> T.transform(): T = when {
        this.isZ3Formula() -> this.z3FormulaTransform(originalContext, secondaryFm).also {
            storage.putIfAbsent(this, it)
        }
        else -> error("can't transform this type formula ${this::class}")
    }
}


















