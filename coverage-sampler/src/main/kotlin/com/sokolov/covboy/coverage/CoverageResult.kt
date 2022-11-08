package com.sokolov.covboy.coverage

import com.sokolov.covboy.prover.Assignment
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.BooleanFormulaManager

data class CoverageResult(
    val atomsCoverage: Set<AtomCoverageBase> = emptySet(),
    val solverCheckCalls: Int = 0,
    val coverageComputationMillis: Long = 0
) : Comparable<CoverageResult> {

    val coverageNumber
        get() = atomsCoverage.sumOf { it.coverageValue } / atomsCoverage.size

    val atomsCount: Int
        get() = atomsCoverage.size

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

    override fun compareTo(other: CoverageResult): Int = compareValuesBy(this, other,
        { it.atomsCount },
        { it.coverageNumber }
    )

    fun diff(other: CoverageResult): Set<Assignment<BooleanFormula>> {
        if (atomsCoverage == other.atomsCoverage)
            return emptySet()

        return buildSet {
            atomsCoverage.forEach { atomCoverage ->
                other.atomsCoverage.find { it.expr == atomCoverage.expr }?.let { otherAtomCov ->
                    val difference = (atomCoverage.values + otherAtomCov.values) - atomCoverage.values
                    addAll(difference.map { Assignment(atomCoverage.expr, it) })
                } ?: addAll(atomCoverage.values.map { Assignment(atomCoverage.expr, it) })
            }
        }
    }
}

sealed class AtomCoverageBase(
    open val expr: BooleanFormula,
    open val values: Set<BooleanFormula>,
) : Comparable<AtomCoverageBase> {
    val coverageValue: Double
        get() = values.size / 2.0 // 2 - power of values (True, False)

    val coveredValuesCount: Int
        get() = values.size

    val isEmpty: Boolean
        get() = values.isEmpty()

    val isFull: Boolean
        get() = values.size == 2


    fun update(newCoverage: AtomCoverageBase): AtomCoverageBase {
        require(expr == newCoverage.expr)

        return when {
            this.isFull -> this
            newCoverage.isFull -> newCoverage
            this is EmptyAtomCoverage -> newCoverage
            newCoverage is EmptyAtomCoverage -> this

            else -> AtomCoverage(expr, values + newCoverage.values)
        }
    }

    fun coverNewValue(value: BooleanFormula): AtomCoverageBase =
        if (value in values) this
        else AtomCoverage(expr, values + value)

    override fun compareTo(other: AtomCoverageBase): Int = if (this.expr != other.expr)
            throw IllegalArgumentException("Unable to compare different expr coverage: $this and $other")
    else compareValuesBy(this, other) { it.coverageValue }
}

data class EmptyAtomCoverage(
    override val expr: BooleanFormula
) : AtomCoverageBase(expr, emptySet()) {
    override fun toString(): String {
        return "EmptyAtomCoverage(${expr.toShortString()})"
    }
}

data class AtomCoverage(
    override val expr: BooleanFormula,
    override val values: Set<BooleanFormula>,
) : AtomCoverageBase(expr, values) {

    override fun toString(): String {
        return "AtomCoverage(expr = ${expr.toShortString()}, values = ${values})"
    }
}

fun FullCoverage(atom: BooleanFormula, formulaManager: BooleanFormulaManager): AtomCoverageBase =
    AtomCoverage(atom, setOf(formulaManager.makeTrue(), formulaManager.makeFalse()))

// Free atom coverage
//class NonEffectingAtomCoverage(atom: BooleanFormula, formulaManager: BooleanFormulaManager) : AtomCoverageBase(
//    atom,
//    setOf(formulaManager.makeTrue(), formulaManager.makeFalse()),
//    false
//) {
//    override fun toString(): String {
//        return "NonEffectingAtomCoverage(${expr.toShortString()})"
//    }
//}

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
