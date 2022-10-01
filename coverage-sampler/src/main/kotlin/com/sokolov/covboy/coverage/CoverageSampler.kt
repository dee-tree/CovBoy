package com.sokolov.covboy.coverage

import com.sokolov.covboy.logger
import com.sokolov.covboy.prover.*
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.FormulaManager
import org.sosy_lab.java_smt.api.Model
import org.sosy_lab.java_smt.api.SolverContext
import kotlin.system.measureTimeMillis

abstract class CoverageSampler(
    protected val context: SolverContext,
    protected val prover: IProver,
    coveragePredicates: Collection<BooleanFormula>
) {

    protected val coveragePredicates: Set<BooleanFormula> = coveragePredicates.toSet()

    protected val formulaManager: FormulaManager = context.formulaManager

    private val coverageEvaluator = CoverageEvaluator(this.coveragePredicates, formulaManager.booleanFormulaManager)

    private var coverageResult: CoverageResult = CoverageResult(emptySet(), 0, 0)

    protected val modelsEnumerator = ModelsEnumerator(prover, formulaManager)

    protected abstract fun computeCoverage(
        coverModel: (Model) -> Set<AtomCoverageBase>,
        coverAtom: (assignment: Assignment<BooleanFormula>) -> AtomCoverageBase,
        onImpossibleAssignmentFound: (assignment: Assignment<BooleanFormula>) -> Unit,
    )

    fun computeCoverage(): CoverageResult {
        if (prover.check() != Status.SAT) {
            System.err.println("Formula is ${prover.check()}. No coverage is available!")
            throw IllegalStateException("Formula is ${prover.check()}. No coverage is available!")
        }

        computeCoverage(::cover, ::coverAtom, ::onImpossibleAssignmentFound)
        println(coverageResult)
        logger().debug("Checks statistics: ${prover.checksStatistics}")
        return coverageResult
    }

    protected val isCovered: Boolean
        get() = coverageEvaluator.isCovered

    protected val atomsWithSingleUncoveredValue: Map<BooleanFormula, BooleanFormula>
        get() = coverageEvaluator.booleansWithSingleUncoveredValue

    protected val uncoveredAtomsWithAnyValue: Set<Assignment<BooleanFormula>>
        get() = coverageEvaluator.uncoveredBooleansWithAnyValue

    protected val firstSemiCoveredAtom: Assignment<BooleanFormula>?
        get() = coverageEvaluator.firstSemiCoveredAtom()

    private fun cover(model: Model): Set<AtomCoverageBase> {
        val modelCoverage: Set<AtomCoverageBase>

        val modelCoverageMillis = measureTimeMillis {
            modelCoverage = coverageEvaluator.cover(model)
        }

        coverageResult = coverageResult.copy(
            atomsCoverage = (coverageResult.atomsCoverage to modelCoverage).merge(),
            coverageComputationMillis = coverageResult.coverageComputationMillis + modelCoverageMillis
        )
        return modelCoverage
    }

    private fun coverAtom(assignment: Assignment<BooleanFormula>): AtomCoverageBase {
        val atomCoverage: AtomCoverageBase

        val atomCoverageMillis =
            measureTimeMillis { atomCoverage = coverageEvaluator.coverAtom(assignment.expr, assignment.value) }

        coverageResult = coverageResult.copy(
            atomsCoverage = (coverageResult.atomsCoverage to setOf(atomCoverage)).merge(),
            coverageComputationMillis = coverageResult.coverageComputationMillis + atomCoverageMillis
        )

        return atomCoverage
    }

    private fun onImpossibleAssignmentFound(assignment: Assignment<BooleanFormula>) {
        val atomCoverage: AtomCoverageBase
        val atomCoverageMillis = measureTimeMillis {
            atomCoverage = coverageEvaluator.excludeFromCoverageArea(assignment)
        }
    }

}

fun <T : CoverageSampler> T.checkStatusId(): String = this::class.simpleName ?: this::class.toString()