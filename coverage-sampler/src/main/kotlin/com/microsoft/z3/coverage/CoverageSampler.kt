package com.microsoft.z3.coverage

import com.microsoft.z3.Assignment
import com.sokolov.smt.Status
import com.sokolov.smt.prover.IProver
import com.sokolov.smt.sampler.logger
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


    protected val customAssertionsStorage: AssertionsStorage =
        AssertionsStorage(prover, ::onAssertionChanged)

    protected val unsatCoreWithAssumptions
        get() = prover.unsatCoreOverAssumptions(customAssertionsStorage.assumptions).get()

    private val coverageEvaluator = CoverageEvaluator(this.coveragePredicates, formulaManager.booleanFormulaManager)

    private var coverageResult: CoverageResult = CoverageResult(emptySet(), 0, 0)

    protected val modelsEnumerator = ModelsEnumerator(
        prover = prover,
        formulaManager = formulaManager,
        assertionsStorage = customAssertionsStorage,
        check = ::checkWithAssumptions
    )


    // checks optimization via last status remember
    private var lastCheckStatus: Status? = null
    protected var isCheckNeed: Boolean = true
        private set

    private val checkCounts = mutableMapOf<String, ChecksCounter>()

    protected fun checkWithAssumptions(reason: String = ""): Status {
        if (!isCheckNeed)
            return lastCheckStatus!!

        coverageResult = coverageResult.copy(solverCheckCalls = coverageResult.solverCheckCalls + 1)

        lastCheckStatus = prover.check(customAssertionsStorage.assumptions)
        isCheckNeed = false

        checkCounts.getOrPut(reason) { ChecksCounter() }.update(lastCheckStatus!!)

        if (lastCheckStatus == Status.UNKNOWN)
            throw IllegalStateException("got unknown status on solver check...")

        return lastCheckStatus!!
    }

    protected abstract fun computeCoverage(
        coverModel: (Model) -> Set<AtomCoverageBase>,
        coverAtom: (assignment: Assignment<BooleanFormula>) -> AtomCoverageBase,
        onImpossibleAssignmentFound: (assignment: Assignment<BooleanFormula>) -> Unit,
    )

    fun computeCoverage(): CoverageResult {
        if (checkWithAssumptions() != Status.SAT) {
            System.err.println("Formula is $lastCheckStatus. No coverage is available!")
            throw IllegalStateException("Formula is $lastCheckStatus. No coverage is available!")
        }

        computeCoverage(::cover, ::coverAtom, ::onImpossibleAssignmentFound)
        println(coverageResult)
        logger().debug("Checks statistics: $checkCounts")
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

    private fun onAssertionChanged(newState: AssertionState) {
        isCheckNeed = true
    }
}

private data class ChecksCounter(
    var sat: Int = 0,
    var unsat: Int = 0,
    var unknown: Int = 0
) {
    fun update(status: Status): Unit = when (status) {
        Status.SAT -> sat += 1
        Status.UNSAT -> unsat += 1
        Status.UNKNOWN -> unknown += 1
    }
}

fun <T : CoverageSampler> T.checkStatusId(): String = this::class.simpleName ?: this::class.toString()