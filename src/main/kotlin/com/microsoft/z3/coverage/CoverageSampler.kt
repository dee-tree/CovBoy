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
    protected val customAssertionsStorage: AssertionsStorage = AssertionsStorage(solver, context)

    protected val coverage = CoverageEvaluator(solver)

    private var coverageResult: CoverageResult = CoverageResult(emptyMap(), 0, 0, 0)

    fun checkWithAssumptions(): Status {
        coverageResult = coverageResult.copy(solverCheckCalls = coverageResult.solverCheckCalls + 1)
        return solver.check(*customAssertionsStorage.assumptions.toTypedArray())
    }

    abstract fun computeCoveringModels(): Collection<Model>

    private fun computeCoverageWithTimeMeasure(): Collection<Model> {
        val models: Collection<Model>
        val coveringModelsComputationMillis = measureTimeMillis { models = computeCoveringModels() }

        coverageResult = coverageResult.copy(coveringModelsComputationMillis = coveringModelsComputationMillis)
        return models
    }

    fun getCoverage(): CoverageResult {
        if (!coverageResult.isEmpty()) return coverageResult

        val atomsCoverage: Map<BoolExpr, Double>
        val coverageComputationMillis = measureTimeMillis {
            atomsCoverage = this.coverage.eval(computeCoverageWithTimeMeasure())
        }

        coverageResult = coverageResult.copy(
            atomsCoverage = atomsCoverage,
            coverageComputationMillis = coverageComputationMillis
        )
        return coverageResult
    }
}