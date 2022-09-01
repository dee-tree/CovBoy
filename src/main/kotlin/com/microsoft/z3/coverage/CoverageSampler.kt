package com.microsoft.z3.coverage

import com.microsoft.z3.BoolExpr
import com.microsoft.z3.Context
import com.microsoft.z3.Model
import com.microsoft.z3.Solver
import com.microsoft.z3.Status
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

    private var satCount = 0
    private var unsatCount = 0

    protected fun checkWithAssumptions(): Status {
        if (!isCheckNeed)
            return lastCheckStatus!!

        coverageResult = coverageResult.copy(solverCheckCalls = coverageResult.solverCheckCalls + 1)

        lastCheckStatus = solver.check(*customAssertionsStorage.assumptions.toTypedArray())
        isCheckNeed = false

        when (lastCheckStatus) {
            Status.SATISFIABLE -> satCount++
            Status.UNSATISFIABLE -> unsatCount++
        }

        return lastCheckStatus!!
    }

    protected abstract fun computeCoverage(
        coverModel: (Model) -> Set<AtomCoverageBase>,
        coverAtom: (atom: BoolExpr, value: BoolExpr) -> AtomCoverageBase,
        onImpossibleAtomValueFound: (atom: BoolExpr, impossibleValue: BoolExpr) -> Unit,
    )

    fun computeCoverage(): CoverageResult {
        computeCoverage(::cover, ::coverAtom, ::onImpossibleAtomValueFound)
        println(coverageResult)
        logger().debug("Total SATs: $satCount (${satCount.toDouble() / (satCount+unsatCount)}); Total UNSATs: $unsatCount (${unsatCount.toDouble() / (satCount+unsatCount)})")
        return coverageResult
    }

    protected val isCovered: Boolean
        get() = coverageEvaluator.isCovered

    protected val atomsWithSingleUncoveredValue: Map<BoolExpr, BoolExpr>
        get() = coverageEvaluator.atomsWithSingleUncoveredValue

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

    private fun onImpossibleAtomValueFound(atom: BoolExpr, impossibleValue: BoolExpr) {
        val atomCoverage: AtomCoverageBase
        val atomCoverageMillis = measureTimeMillis { atomCoverage = coverageEvaluator.coverAtom(atom, impossibleValue) }
    }

    private fun onAssertionChanged(newState: AssertionState) {
        isCheckNeed = true
    }
}