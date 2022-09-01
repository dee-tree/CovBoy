package com.microsoft.z3.coverage

import com.microsoft.z3.*

class CoverageEvaluator(
    solver: Solver,
    private val context: Context
) {

    private val atoms = solver.atoms

    private val uncoveredValues = mutableMapOf<BoolExpr, MutableSet<BoolExpr>>()

    val isCovered: Boolean
        get() = uncoveredValues.all { it.value.isEmpty() }

    init {
        atoms.forEach { atom ->
            uncoveredValues[atom] = mutableSetOf(context.mkTrue(), context.mkFalse())
        }
    }

    /**
     * @return covered by this call atoms with their values
     */
    fun cover(model: Model): Set<AtomCoverageBase> {
        val thisModelCoverage = mutableSetOf<AtomCoverageBase>()

        var covered = 0
        var uncovered = 0

        atoms.forEach { atom ->
            val atomValue = model.eval(atom, false) as BoolExpr
            val coveredThisAtom = coverAtom(atom, atomValue)
            if (coveredThisAtom !is EmptyAtomCoverage) covered++ else uncovered++

            thisModelCoverage.add(coveredThisAtom)

        }

        return thisModelCoverage
    }

    fun firstSemiCoveredAtom(): Pair<BoolExpr, BoolExpr>? = uncoveredValues.entries
        .firstOrNull { it.value.size == 1 }
        ?.let { it.key to it.value.first() }

    fun coverAtom(atom: BoolExpr, value: BoolExpr): AtomCoverageBase {
        if (!value.isCertainBool) {
            return if (uncoveredValues[atom]?.isNotEmpty() == true) {
                // in case of atom is free (not important atom)
                removeFromUncovered(value)
                NonEffectingAtomCoverage(atom, context)
            } else {
                EmptyAtomCoverage(atom)
            }
        }

        if (!removeFromUncovered(atom, value)) return EmptyAtomCoverage(atom)
        return AtomCoverage(atom, setOf(value))
    }


    /**
     * @return true, if value was removed successful, false if uncoveredValues did not contain value
     */
    private fun removeFromUncovered(atom: BoolExpr, value: BoolExpr): Boolean {
        val covered = uncoveredValues[atom]?.remove(value) ?: return false

        if (uncoveredValues[atom]?.isEmpty() == true) {
            uncoveredValues.remove(atom)
        }

        return covered
    }

    private fun removeFromUncovered(atom: BoolExpr) {
        uncoveredValues[atom]?.clear()
        uncoveredValues.remove(atom)
    }

}