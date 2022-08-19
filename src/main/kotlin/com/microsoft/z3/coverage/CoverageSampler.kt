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

    var solverCheckCalls: Int = 0
        private set

    var coveringModelsComputationMillis: Long = 0
        private set

    var coverageComputationMillis: Long = 0
        private set

    fun checkWithAssumptions(): Status {
        solverCheckCalls++
        return solver.check(*customAssertionsStorage.assumptions.toTypedArray())
    }

    abstract fun computeCoveringModels(): Collection<Model>

    private fun computeCoverageWithTimeMeasure(): Collection<Model> {
        val models: Collection<Model>
        coveringModelsComputationMillis = measureTimeMillis { models = computeCoveringModels() }
        return models
    }

    fun getCoverage(): Map<BoolExpr, Double> {
        val coverage: Map<BoolExpr, Double>

        coverageComputationMillis = measureTimeMillis {
            coverage = this.coverage.eval(computeCoverageWithTimeMeasure())
        }
        return coverage
    }

    fun printCoverage() {
        val coverage = getCoverage()

        println("\n")
        println("${"-".repeat(5)} Coverage statistics ${"-".repeat(5)}")
        println("\t * Coverage computation measured (without final models handling): $coveringModelsComputationMillis ms")
        println("\t * Coverage computation measured (totally): $coverageComputationMillis ms")
        println("\t * \"solver-check\" calls: $solverCheckCalls")
        println()
        println(coverage)
    }
}