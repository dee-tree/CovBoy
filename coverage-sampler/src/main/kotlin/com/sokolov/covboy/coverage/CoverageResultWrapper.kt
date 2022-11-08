package com.sokolov.covboy.coverage

import org.sosy_lab.java_smt.SolverContextFactory.Solvers

@kotlinx.serialization.Serializable
class CoverageResultWrapper private constructor(
    val solver: Solvers,
    val coveragePredicatesCount: Int,
    val coveredValuesPortion: Double,
    val predicatesCoverage: Set<PredicateCoverageWrapper>
) : Comparable<CoverageResultWrapper> {

    companion object {
        fun fromCoverageResult(solver: Solvers, coverageResult: CoverageResult): CoverageResultWrapper {
            return CoverageResultWrapper(
                solver,
                coverageResult.atomsCount,
                coverageResult.coverageNumber,
                coverageResult.atomsCoverage.map { PredicateCoverageWrapper.fromAtomCoverage(it) }.toSet()
            )
        }

        fun diffAsString(baseResult: CoverageResultWrapper, anotherResult: CoverageResultWrapper): String =
            "$baseResult\n$anotherResult\n" +
                    if (baseResult < anotherResult) {
                        "Base coverage < another coverage!\n" +
                                "Difference: ${baseResult.diff(anotherResult)}"
                    } else {
                        "Base coverage > another coverage!\n" +
                                "Difference: ${baseResult.diff(anotherResult)}"
                    }
    }

    override fun compareTo(other: CoverageResultWrapper): Int = compareValuesBy(this, other,
        { it.coveragePredicatesCount },
        { it.coveredValuesPortion }
    )

    fun diff(other: CoverageResultWrapper): Set<String> {
        if (coveragePredicatesCount == other.coveragePredicatesCount)
            return emptySet()

        return buildSet {
            predicatesCoverage.forEach { atomCoverage ->
                other.predicatesCoverage.find { it.expr == atomCoverage.expr }?.let { otherAtomCov ->
                    val difference =
                        (atomCoverage.coveredValues + otherAtomCov.coveredValues) - atomCoverage.coveredValues
                    addAll(difference.map { "${atomCoverage.expr} = $it" })
                } ?: addAll(atomCoverage.coveredValues.map { "${atomCoverage.expr} = $it" })
            }
        }
    }

}

@kotlinx.serialization.Serializable
class PredicateCoverageWrapper private constructor(
    val expr: String,
    val coveredValues: Set<String>,
    val coveragePortion: Double
) {

    companion object {
        fun fromAtomCoverage(coverage: AtomCoverageBase): PredicateCoverageWrapper {
            return PredicateCoverageWrapper(
                coverage.expr.toString(),
                coverage.values.map { it.toString() }.toSet(),
                coverage.coverageValue
            )
        }
    }
}