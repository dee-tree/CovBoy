package com.microsoft.z3.coverage

import com.microsoft.z3.BoolExpr

data class CoverageResult(
    val atomsCoverage: Map<BoolExpr, Double> = emptyMap(),
    val solverCheckCalls: Int = 0,
    val coveringModelsComputationMillis: Long = 0,
    val coverageComputationMillis: Long = 0
) {

    val coverageNumber
        get() = atomsCoverage.values.sum() / atomsCoverage.values.size

    fun isEmpty(): Boolean = atomsCoverage.isEmpty()

    fun asStringInfo(): String = """
        ${"-".repeat(5)} Coverage statistics ${"-".repeat(5)}
        ${"\t"} * Coverage computation measured (without final models handling): $coveringModelsComputationMillis ms
        ${"\t"} * Coverage computation measured (totally): $coverageComputationMillis ms
        ${"\t"} * \"solver-check\" calls: $solverCheckCalls
        
        Coverage number: $coverageNumber
    """.trimIndent()

}