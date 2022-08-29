package com.microsoft.z3.coverage

import com.microsoft.z3.BoolExpr
import com.microsoft.z3.Context
import com.microsoft.z3.Model
import com.microsoft.z3.Solver
import com.microsoft.z3.Status
import kotlin.system.measureTimeMillis

abstract class CoverageSampler(
    protected val solver: Solver,
    protected val context: Context
) {
    protected val customAssertionsStorage: AssertionsStorage = AssertionsStorage(solver, context, ::onAssertionChanged)

    private val coverage = CoverageEvaluator(solver, context)

    private var coverageResult: CoverageResult = CoverageResult(emptySet(), 0, 0)


    // checks optimization
    private var lastCheckStatus: Status? = null
    var isCheckNeed: Boolean = true
        private set

    fun checkWithAssumptions(): Status {
        if (!isCheckNeed)
            return lastCheckStatus!!

        coverageResult = coverageResult.copy(solverCheckCalls = coverageResult.solverCheckCalls + 1)

        lastCheckStatus = solver.check(*customAssertionsStorage.assumptions.toTypedArray())
        isCheckNeed = false
        return lastCheckStatus!!
    }

    protected abstract fun computeCoverage(
        coverModel: (Model) -> Set<AtomCoverageBase>,
        coverAtom: (atom: BoolExpr, value: BoolExpr) -> AtomCoverageBase
    )

    fun computeCoverage(): CoverageResult {
        computeCoverage(::cover, ::coverAtom)
        println("Coverage result: $coverageResult")
        return coverageResult
    }

    protected val isCovered: Boolean
        get() = coverage.isCovered

    protected val firstSemiCoveredAtom: Pair<BoolExpr, BoolExpr>?
        get() = coverage.firstSemiCoveredAtom()

    private fun cover(model: Model): Set<AtomCoverageBase> {
        val modelCoverage: Set<AtomCoverageBase>

        val modelCoverageMillis = measureTimeMillis {
            modelCoverage = coverage.cover(model)
        }

        coverageResult = coverageResult.copy(
            atomsCoverage = (coverageResult.atomsCoverage to modelCoverage).merge(),
            coverageComputationMillis = coverageResult.coverageComputationMillis + modelCoverageMillis
        )
        return modelCoverage
    }

    private fun coverAtom(atom: BoolExpr, value: BoolExpr): AtomCoverageBase {
        val atomCoverage: AtomCoverageBase

        val atomCoverageMillis = measureTimeMillis { atomCoverage = coverage.coverAtom(atom, value) }

        coverageResult = coverageResult.copy(
            atomsCoverage = (coverageResult.atomsCoverage to setOf(atomCoverage)).merge(),
            coverageComputationMillis = coverageResult.coverageComputationMillis + atomCoverageMillis
        )

        return atomCoverage
    }

    private fun onAssertionChanged(newState: AssertionState) {
        isCheckNeed = true
    }
}