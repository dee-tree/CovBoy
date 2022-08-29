package com.microsoft.z3.coverage

import com.microsoft.z3.BoolExpr
import com.microsoft.z3.Context

data class CoverageResult(
    val atomsCoverage: Set<AtomCoverageBase> = emptySet(),
    val solverCheckCalls: Int = 0,
    val coverageComputationMillis: Long = 0
) {

    val coverageNumber
        get() = atomsCoverage.sumOf { it.coverageValue } / atomsCoverage.size

    fun isEmpty(): Boolean = atomsCoverage.all { it.isEmpty }

    fun asStringInfo(): String = """
        ${"-".repeat(5)} Coverage statistics ${"-".repeat(5)}
        ${"\t"} * Coverage computation measured (totally): $coverageComputationMillis ms
        ${"\t"} * \"solver-check\" calls: $solverCheckCalls
        
        Coverage number: $coverageNumber
    """.trimIndent()
}

sealed class AtomCoverageBase(
    open val atom: BoolExpr,
    open val values: Set<BoolExpr>,
    open val definedBySolver: Boolean // means that this atom affect the formula
) {
    val coverageValue: Double
        get() = values.size / 2.0 // 2 - power of values (True, False)

    val coveredValuesCount: Int
        get() = values.size

    val isEmpty: Boolean
        get() = values.isEmpty()


    fun update(newCoverage: AtomCoverageBase): AtomCoverageBase {
        require(atom == newCoverage.atom)

        return when {
            this is NonEffectingAtomCoverage -> this
            newCoverage is NonEffectingAtomCoverage -> newCoverage
            this is EmptyAtomCoverage -> newCoverage
            newCoverage is EmptyAtomCoverage -> this

            else -> AtomCoverage(atom, values + newCoverage.values)
        }
    }

    fun coverNewValue(value: BoolExpr): AtomCoverageBase =
        if (value in values) this
        else AtomCoverage(atom, values + value)
}

data class EmptyAtomCoverage(
    override val atom: BoolExpr
) : AtomCoverageBase(atom, emptySet(), true)

data class AtomCoverage(
    override val atom: BoolExpr,
    override val values: Set<BoolExpr>,
) : AtomCoverageBase(atom, values, true) {

}

class NonEffectingAtomCoverage(atom: BoolExpr, context: Context) : AtomCoverageBase(
    atom,
    setOf(context.mkTrue(), context.mkFalse()),
    false
) {

}


fun Pair<Collection<AtomCoverageBase>, Collection<AtomCoverageBase>>.merge(): Set<AtomCoverageBase> = buildSet {

    val intersectedAtoms = first.map { it.atom }.intersect(second.map { it.atom }.toSet())


    intersectedAtoms.forEach { intersectedAtom ->
        add(first.first { it.atom == intersectedAtom }.update(second.first { it.atom == intersectedAtom }))
    }

    addAll((first - first.mapNotNull { if (it.atom in intersectedAtoms) it else null }
        .toSet()) + (second - second.mapNotNull { if (it.atom in intersectedAtoms) it else null }.toSet()))
}
