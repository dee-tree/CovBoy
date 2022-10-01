package com.sokolov.covboy.coverage

import com.sokolov.covboy.prover.Assignment
import com.sokolov.covboy.smt.isCertainBool
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.BooleanFormulaManager
import org.sosy_lab.java_smt.api.Model

class CoverageEvaluator(
    private val coveragePredicates: Set<BooleanFormula>,
    private val formulaManager: BooleanFormulaManager
) {

    private val uncoveredValues = mutableMapOf<BooleanFormula, MutableSet<BooleanFormula>>()

    val isCovered: Boolean
        get() = uncoveredValues.all { it.value.isEmpty() }

    val booleansWithSingleUncoveredValue: Map<BooleanFormula, BooleanFormula>
        get() = uncoveredValues.entries.filter { it.value.size == 1 }.associate { it.key to it.value.first() }

    val uncoveredBooleansWithAnyValue: Set<Assignment<BooleanFormula>>
        get() = uncoveredValues.entries.map { Assignment(it.key, it.value.random()) }.toSet()


    init {
        coveragePredicates.forEach { boolExpr ->
            uncoveredValues[boolExpr] = mutableSetOf(formulaManager.makeTrue(), formulaManager.makeFalse())
        }
    }

    /**
     * @return covered by this call atoms with their values
     */
    fun cover(model: Model): Set<AtomCoverageBase> {
        val thisModelCoverage = mutableSetOf<AtomCoverageBase>()

        var covered = 0
        var uncovered = 0

        coveragePredicates.forEach { boolExpr ->
            //TODO here must be incomplete model eval
            val atomValue = model.evaluate(boolExpr)?.let { formulaManager.makeBoolean(it) } ?: boolExpr
            val coveredThisAtom = coverAtom(boolExpr, atomValue)
            if (coveredThisAtom !is EmptyAtomCoverage) covered++ else uncovered++

            thisModelCoverage.add(coveredThisAtom)

        }

        return thisModelCoverage
    }

    fun firstSemiCoveredAtom(): Assignment<BooleanFormula>? = uncoveredValues.entries
        .firstOrNull { it.value.size == 1 }
        ?.let { Assignment(it.key, it.value.first()) }


    fun excludeFromCoverageArea(atom: Assignment<BooleanFormula>): AtomCoverageBase {
        return coverAtom(atom.expr, atom.value)
    }

    fun coverAtom(atom: BooleanFormula, value: BooleanFormula): AtomCoverageBase {
        if (!value.isCertainBool(formulaManager)) {
            return if (uncoveredValues[atom]?.isNotEmpty() == true) {
                // in case of atom is free (not important atom)
                removeFromUncovered(atom)
                NonEffectingAtomCoverage(atom, formulaManager)
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
    private fun removeFromUncovered(atom: BooleanFormula, value: BooleanFormula): Boolean {
        val covered = uncoveredValues[atom]?.remove(value) ?: return false

        if (uncoveredValues[atom]?.isEmpty() == true) {
            uncoveredValues.remove(atom)
        }

        return covered
    }

    private fun removeFromUncovered(atom: BooleanFormula) {
        uncoveredValues[atom]?.clear()
        uncoveredValues.remove(atom)
    }

}