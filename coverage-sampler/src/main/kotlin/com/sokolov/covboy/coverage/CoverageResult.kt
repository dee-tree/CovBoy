package com.sokolov.covboy.coverage

import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.BooleanFormulaManager

data class CoverageResult(
    val atomsCoverage: Set<AtomCoverageBase> = emptySet(),
    val solverCheckCalls: Int = 0,
    val coverageComputationMillis: Long = 0
) {

    val coverageNumber
        get() = atomsCoverage.sumOf { it.coverageValue } / atomsCoverage.size

    val atomsCount: Int
        get() = atomsCoverage.size

    val freeAtoms: List<AtomCoverageBase>
        get() = atomsCoverage.filterIsInstance<NonEffectingAtomCoverage>()

    val freeAtomsPortion: Double
        get() = freeAtoms.size / atomsCoverage.size.toDouble()

    fun isEmpty(): Boolean = atomsCoverage.all { it.isEmpty }

    fun asStringInfo(): String = """
        ${"-".repeat(5)} Coverage result ${"-".repeat(5)}
        ${"\t"} * Coverage computation measured (totally): $coverageComputationMillis ms
        ${"\t"} * \"solver-check\" calls: $solverCheckCalls
        
        Coverage number: $coverageNumber
        Coverage per atom:
        ${atomsCoverage.joinToString(separator = "\n") { "\t - $it" }}
        ${"-".repeat(15)}
    """.trimIndent()

    override fun toString(): String = asStringInfo()
}

sealed class AtomCoverageBase(
    open val expr: BooleanFormula,
    open val values: Set<BooleanFormula>,
    open val definedBySolver: Boolean // means that this atom affect the formula
) {
    val coverageValue: Double
        get() = values.size / 2.0 // 2 - power of values (True, False)

    val coveredValuesCount: Int
        get() = values.size

    val isEmpty: Boolean
        get() = values.isEmpty()


    fun update(newCoverage: AtomCoverageBase): AtomCoverageBase {
        require(expr == newCoverage.expr)

        return when {
            this is NonEffectingAtomCoverage -> this
            newCoverage is NonEffectingAtomCoverage -> newCoverage
            this is EmptyAtomCoverage -> newCoverage
            newCoverage is EmptyAtomCoverage -> this

            else -> AtomCoverage(expr, values + newCoverage.values)
        }
    }

    fun coverNewValue(value: BooleanFormula): AtomCoverageBase =
        if (value in values) this
        else AtomCoverage(expr, values + value)
}

data class EmptyAtomCoverage(
    override val expr: BooleanFormula
) : AtomCoverageBase(expr, emptySet(), true) {
    override fun toString(): String {
        return "EmptyAtomCoverage(${expr.toShortString()})"
    }
}

data class AtomCoverage(
    override val expr: BooleanFormula,
    override val values: Set<BooleanFormula>,
) : AtomCoverageBase(expr, values, true) {

    override fun toString(): String {
        return "AtomCoverage(expr = ${expr.toShortString()}, values = ${values})"
    }
}

// Free atom coverage
class NonEffectingAtomCoverage(atom: BooleanFormula, formulaManager: BooleanFormulaManager) : AtomCoverageBase(
    atom,
    setOf(formulaManager.makeTrue(), formulaManager.makeFalse()),
    false
) {
    override fun toString(): String {
        return "NonEffectingAtomCoverage(${expr.toShortString()})"
    }
}

private fun BooleanFormula.toShortString(): String {
    val exprRepresentation = this.toString()
    return if (exprRepresentation.length < 50) exprRepresentation else exprRepresentation.take(40) + "... (hash: ${this.hashCode()})"
}

fun Pair<Collection<AtomCoverageBase>, Collection<AtomCoverageBase>>.merge(): Set<AtomCoverageBase> = buildSet {

    val intersectedAtoms = first.map { it.expr }.intersect(second.map { it.expr }.toSet())

    intersectedAtoms.forEach { intersectedAtom ->
        add(first.first { it.expr == intersectedAtom }.update(second.first { it.expr == intersectedAtom }))
    }

    addAll((first - first.mapNotNull { if (it.expr in intersectedAtoms) it else null }
        .toSet()) + (second - second.mapNotNull { if (it.expr in intersectedAtoms) it else null }.toSet()))
}
