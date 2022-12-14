package com.sokolov.covboy.coverage

import com.sokolov.covboy.solvers.formulas.utils.isBooleanLiteral
import com.sokolov.covboy.utils.makeAssignment
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.BooleanFormulaManager
import org.sosy_lab.java_smt.api.Model
import org.sosy_lab.java_smt.api.Model.ValueAssignment

class CoverageEvaluator(
    private val coveragePredicates: Set<BooleanFormula>,
    private val formulaManager: BooleanFormulaManager
) {

//    private val uncoveredValues = mutableMapOf<BooleanFormula, MutableSet<BooleanFormula>>()
    private val uncoveredValues = mutableMapOf<BooleanFormula, MutableSet<ValueAssignment>>()

    val isCovered: Boolean
        get() = uncoveredValues.all { it.value.isEmpty() }

    val booleansWithSingleUncoveredValue: List<ValueAssignment>
        get() = uncoveredValues.entries.filter { it.value.size == 1 }.map { it.value.first() }

    val uncoveredBooleansWithAnyValue: Set<ValueAssignment>
        get() = uncoveredValues.entries.map { it.value.random() }.toSet()

    val uncoveredValuesCount: Double
        get() = uncoveredValues.values.sumOf { it.size }.toDouble()


    init {
        coveragePredicates.forEach { boolExpr ->
            val trueAssignment = (boolExpr to formulaManager.makeTrue()).makeAssignment(formulaManager)
            val falseAssignment = (boolExpr to formulaManager.makeFalse()).makeAssignment(formulaManager)

            uncoveredValues[boolExpr] = mutableSetOf(trueAssignment, falseAssignment)
        }
    }

    /**
     * @return covered by this call atoms with their values
     */
    fun cover(model: List<Model.ValueAssignment>): Set<AtomCoverageBase> {
        val thisModelCoverage = mutableSetOf<AtomCoverageBase>()

        var covered = 0
        var uncovered = 0

        coveragePredicates.forEach { boolExpr ->
            val atomValue = model.firstOrNull { it.key == boolExpr }?.valueAsFormula as? BooleanFormula ?: boolExpr
            val coveredThisAtom = coverAtom(boolExpr, atomValue)
            if (coveredThisAtom !is EmptyAtomCoverage) covered++ else uncovered++

            thisModelCoverage.add(coveredThisAtom)

        }

        return thisModelCoverage
    }

    fun firstSemiCoveredAtom(): ValueAssignment? = uncoveredValues.entries
        .firstOrNull { it.value.size == 1 }?.value?.first()


    fun excludeFromCoverageArea(atom: ValueAssignment): AtomCoverageBase {
        return coverAtom(atom.key as BooleanFormula, atom.valueAsFormula as BooleanFormula)
    }

    fun coverAtom(atom: BooleanFormula, value: BooleanFormula): AtomCoverageBase {
        if (!formulaManager.isBooleanLiteral(value)) {
            return if (uncoveredValues[atom]?.isNotEmpty() == true) {
                // in case of atom is free (not important atom)
                removeFromUncovered(atom)
                FullCoverage(atom, formulaManager)
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
        val covered = uncoveredValues[atom]?.remove(uncoveredValues[atom]?.find { it.valueAsFormula == value }) ?: return false

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