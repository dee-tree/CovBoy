package com.sokolov.covboy.coverage.sampler

import com.sokolov.covboy.coverage.*
import com.sokolov.covboy.logger
import com.sokolov.covboy.solvers.provers.Prover
import com.sokolov.covboy.solvers.provers.Status
import com.sokolov.covboy.solvers.provers.secondary.SecondaryProver
import com.sokolov.covboy.utils.getPrimaryCoverage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.FormulaManager
import org.sosy_lab.java_smt.api.Model
import kotlin.system.measureTimeMillis

abstract class CoverageSampler(
    protected val prover: Prover,
    coveragePredicates: Collection<BooleanFormula>
) {

    protected val coveragePredicates: Set<BooleanFormula> = coveragePredicates.toSet()

    protected val formulaManager: FormulaManager
        get() = prover.fm

    private val coverageEvaluator = CoverageEvaluator(this.coveragePredicates, formulaManager.booleanFormulaManager)

    private var coverageResult: CoverageResult = CoverageResult(prover, emptySet(), 0, 0)

    protected val modelsEnumerator = ModelsEnumerator(prover)

    protected val uncoveredValuesCount: Double
        get() = coverageEvaluator.uncoveredValuesCount

    protected abstract fun computeCoverage(
        coverModel: (List<Model.ValueAssignment>) -> Set<AtomCoverageBase>,
        coverAtom: (assignment: Model.ValueAssignment) -> AtomCoverageBase,
        onImpossibleAssignmentFound: (assignment: Model.ValueAssignment) -> Unit,
    )

    fun computeCoverage(): CoverageResult {
        val checkStatus = prover.checkSat()
        if (checkStatus != Status.SAT) {
            System.err.println("Formula is $checkStatus. No coverage is available!")
            throw IllegalStateException("Formula is $checkStatus. No coverage is available!")
        }

        val initialUncoveredValues = uncoveredValuesCount
        val progressPrinter = GlobalScope.launch {
            while (true) {
                println("Remain uncovered values: $uncoveredValuesCount / $initialUncoveredValues")
                delay(1000)
            }
        }

        computeCoverage(::cover, ::coverAtom, ::onImpossibleAssignmentFound)
        progressPrinter.cancel(CancellationException("Coverage collected"))
        logger().debug("Checks statistics: ${prover.checkCounter}")
        return if (prover is SecondaryProver) prover.getPrimaryCoverage(coverageResult) else coverageResult
    }

    protected val isCovered: Boolean
        get() = coverageEvaluator.isCovered

    protected val atomsWithSingleUncoveredValue: List<Model.ValueAssignment>
        get() = coverageEvaluator.booleansWithSingleUncoveredValue

    protected val uncoveredAtomsWithAnyValue: Set<Model.ValueAssignment>
        get() = coverageEvaluator.uncoveredBooleansWithAnyValue

    protected val firstSemiCoveredAtom: Model.ValueAssignment?
        get() = coverageEvaluator.firstSemiCoveredAtom()

    private fun cover(model: List<Model.ValueAssignment>): Set<AtomCoverageBase> {
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

    private fun coverAtom(assignment: Model.ValueAssignment): AtomCoverageBase {
        val atomCoverage: AtomCoverageBase

        val atomCoverageMillis = measureTimeMillis {
            atomCoverage = coverageEvaluator.coverAtom(assignment.key as BooleanFormula, assignment.valueAsFormula as BooleanFormula)
        }

        coverageResult = coverageResult.copy(
            atomsCoverage = (coverageResult.atomsCoverage to setOf(atomCoverage)).merge(),
            coverageComputationMillis = coverageResult.coverageComputationMillis + atomCoverageMillis
        )

        return atomCoverage
    }

    private fun onImpossibleAssignmentFound(assignment: Model.ValueAssignment) {
        val atomCoverage: AtomCoverageBase
        val atomCoverageMillis = measureTimeMillis {
            atomCoverage = coverageEvaluator.excludeFromCoverageArea(assignment)
        }
    }

    protected fun ModelsEnumerator.take(count: Int): List<List<Model.ValueAssignment>> =
        this.take(coveragePredicates, count)

}

fun <T : CoverageSampler> T.checkStatusId(): String = this::class.simpleName ?: this::class.toString()