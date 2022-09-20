package com.microsoft.z3.coverage

import com.microsoft.z3.*
import com.sokolov.z3cov.logger
import kotlin.system.measureTimeMillis

abstract class CoverageSampler(
    protected val solver: Solver,
    protected val context: Context
) {
    protected val customAssertionsStorage: AssertionsStorage = AssertionsStorage(solver, context, ::onAssertionChanged)

    private val coverageEvaluator = CoverageEvaluator(solver, context)

    private var coverageResult: CoverageResult = CoverageResult(emptySet(), 0, 0)

    protected val modelsEnumerator = ModelsEnumerator(
        solver = solver,
        context = context,
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

        lastCheckStatus = solver.check(*customAssertionsStorage.assumptions.toTypedArray()) ?: throw IllegalStateException("check status is null")
        isCheckNeed = false

        checkCounts.getOrPut(reason) { ChecksCounter() }.update(lastCheckStatus!!)

        if (lastCheckStatus == Status.UNKNOWN)
            throw IllegalStateException("got unknown status on solver check...")

        return lastCheckStatus!!
    }

    protected abstract fun computeCoverage(
        coverModel: (Model) -> Set<AtomCoverageBase>,
        coverAtom: (atom: BoolExpr, value: BoolExpr) -> AtomCoverageBase,
        onImpossibleAssignmentFound: (assignment: Assignment<BoolExpr>) -> Unit,
    )

    fun computeCoverage(): CoverageResult {
        if (checkWithAssumptions() != Status.SATISFIABLE) {
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

    protected val atomsWithSingleUncoveredValue: Map<BoolExpr, BoolExpr>
        get() = coverageEvaluator.atomsWithSingleUncoveredValue

    protected val uncoveredAtomsWithAnyValue: Set<Assignment<BoolExpr>>
        get() = coverageEvaluator.uncoveredAtomsWithAnyValue

    protected val firstSemiCoveredAtom: Pair<BoolExpr, BoolExpr>?
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

    private fun coverAtom(atom: BoolExpr, value: BoolExpr): AtomCoverageBase {
        val atomCoverage: AtomCoverageBase

        val atomCoverageMillis = measureTimeMillis { atomCoverage = coverageEvaluator.coverAtom(atom, value) }

        coverageResult = coverageResult.copy(
            atomsCoverage = (coverageResult.atomsCoverage to setOf(atomCoverage)).merge(),
            coverageComputationMillis = coverageResult.coverageComputationMillis + atomCoverageMillis
        )

        return atomCoverage
    }

    private fun onImpossibleAssignmentFound(assignment: Assignment<BoolExpr>) {
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
        Status.SATISFIABLE -> sat += 1
        Status.UNSATISFIABLE -> unsat += 1
        Status.UNKNOWN -> unknown += 1
    }
}

fun <T : CoverageSampler> T.checkStatusId(): String = this::class.simpleName ?: this::class.toString()