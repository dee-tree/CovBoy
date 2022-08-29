package com.microsoft.z3.coverage

import com.microsoft.z3.*
import com.sokolov.z3cov.logger

class CoverageEvaluator(
    solver: Solver,
    private val context: Context
) {

    private val atoms = solver.atoms

    private val uncoveredValues = mutableMapOf<BoolExpr, MutableSet<BoolExpr>>()

    val isCovered: Boolean
        get() = uncoveredValues.all { it.value.isEmpty() }

    private val True: BoolExpr = context.mkTrue()
    private val False: BoolExpr = context.mkFalse()

    init {
        atoms.forEach { atom ->
            uncoveredValues[atom] = mutableSetOf(True, False)
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

//            if (coveredThisAtom !is EmptyAtomCoverage) {
//                thisModelCoverage.add(coveredThisAtom)
//            }

        }

        logger().debug("Covered: $covered; uncovered: $uncovered")
        logger().trace("Covered atoms: $thisModelCoverage")
        return thisModelCoverage
    }

    fun firstSemiCoveredAtom(): Pair<BoolExpr, BoolExpr>? = uncoveredValues.entries
        .firstOrNull { it.value.size == 1 }
        ?.let { it.key to it.value.first() }

    fun coverAtom(atom: BoolExpr, value: BoolExpr): AtomCoverageBase {
        if (!value.isCertainBool) return NonEffectingAtomCoverage(atom, context)

        val covered = uncoveredValues[atom]?.remove(value) ?: return EmptyAtomCoverage(atom)

        if (!covered) return EmptyAtomCoverage(atom)

        if (uncoveredValues[atom]?.isEmpty() == true) {
            uncoveredValues.remove(atom)
        }
        return AtomCoverage(atom, setOf(value))
    }

}