package com.sokolov.covboy.coverage

import com.sokolov.covboy.logger
import com.sokolov.covboy.prover.Assignment
import com.sokolov.covboy.prover.BaseProverEnvironment
import com.sokolov.covboy.prover.Status
import com.sokolov.covboy.prover.model.ModelAssignments
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.FormulaManager
import kotlin.system.measureTimeMillis

abstract class CoverageSampler(
    protected val prover: BaseProverEnvironment,
    coveragePredicates: Collection<BooleanFormula>
) {

    protected val coveragePredicates: Set<BooleanFormula> = coveragePredicates.toSet()

    protected val formulaManager: FormulaManager = prover.context.formulaManager

    private val coverageEvaluator = CoverageEvaluator(this.coveragePredicates, formulaManager.booleanFormulaManager)

    private var coverageResult: CoverageResult = CoverageResult(emptySet(), 0, 0)

    protected val modelsEnumerator = ModelsEnumerator(prover, formulaManager)

    protected abstract fun computeCoverage(
        coverModel: (ModelAssignments<BooleanFormula>) -> Set<AtomCoverageBase>,
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
        logger().debug("Checks statistics: ${prover.checkCounter}")
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

    private fun cover(model: ModelAssignments<BooleanFormula>): Set<AtomCoverageBase> {
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

    protected fun ModelsEnumerator.take(count: Int): List<ModelAssignments<BooleanFormula>> =
        this.take(coveragePredicates, count)

}

fun <T : CoverageSampler> T.checkStatusId(): String = this::class.simpleName ?: this::class.toString()