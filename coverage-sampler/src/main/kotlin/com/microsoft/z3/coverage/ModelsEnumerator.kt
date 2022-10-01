package com.microsoft.z3.coverage

import com.microsoft.z3.Assignment
import com.sokolov.smt.*
import com.sokolov.smt.prover.IProver
import org.sosy_lab.java_smt.api.*

class ModelsEnumerator(
    private val prover: IProver,
    private val formulaManager: FormulaManager,
    private val assertionsStorage: AssertionsStorage,
    private val check: () -> Status = prover::check
) {
    private lateinit var current: Model

    private val predicates = prover.booleans

    var traversedModelsCount = 0
        private set

    fun hasNext(): Boolean = check() == Status.SAT


    fun nextModel(onModel: (Model) -> Unit) {
        current = prover.model

        onModel(current)

        // get incomplete models to avoid "unknown"/"undefined" predicates
        val currentConstraints = predicates
            .map { it to current.evaluate(it) } // TODO: keep in mind that model's eval must be incomplete producer
            .mapNotNull { if (it.second == null) null else Assignment(it.first, formulaManager.booleanFormulaManager.makeBoolean(it.second!!)) }
            .filter { it.value.isCertainBool(formulaManager.booleanFormulaManager) }
            .mergeWithAnd(formulaManager.booleanFormulaManager)

        assertionsStorage.assert(currentConstraints.not(formulaManager.booleanFormulaManager), true)
            .also { if (!it.enabled) it.enable() }
        traversedModelsCount++
    }

    fun take(count: Int): List<Model> = buildList {
        if (count < 1) return@buildList

        repeat(count) {
            if (!hasNext())
                return@buildList
            nextModel { add(it) }
        }
    }

    fun all(): List<Model> = buildList {
        while (hasNext())
            nextModel { add(it) }
    }

    fun forEach(action: (Model) -> Unit) {
        while (hasNext())
            nextModel(action)
    }
}

internal fun Collection<Pair<BooleanFormula, Formula>>.mergeWith(
    merger: (Array<out BooleanFormula>) -> BooleanFormula,
    formulaManager: BooleanFormulaManager
): BooleanFormula {
    if (size == 1) return first().let {
        when {
            it.second.isFalse(formulaManager) -> it.first.not(formulaManager)
            else -> it.first
        }
    }

    return merger(
        (filter { it.second.isTrue(formulaManager) }.map { it.first }
                + filter { it.second.isFalse(formulaManager) }.map { it.first.not(formulaManager) }
                ).toTypedArray()
    )
}

@JvmName("mergeWithAssignmentOfBooleanFormula")
internal fun Collection<Assignment<BooleanFormula>>.mergeWith(
    merger: (Array<out BooleanFormula>) -> BooleanFormula,
    formulaManager: BooleanFormulaManager
): BooleanFormula {
    if (size == 1) return first().let {
        when {
            it.value.isFalse(formulaManager) -> it.expr.not(formulaManager)
            else -> it.expr
        }
    }

    return merger(
        (filter { it.value.isTrue(formulaManager) }.map { it.expr }
                + filter { it.value.isFalse(formulaManager) }.map { it.expr.not(formulaManager) }
                ).toTypedArray()
    )
}


internal fun Collection<Pair<BooleanFormula, Formula>>.mergeWithAnd(formulaManager: BooleanFormulaManager): BooleanFormula = mergeWith(formulaManager::and, formulaManager)

@JvmName("mergeWithAndAssignmentOfBooleanFormula")
internal fun Collection<Assignment<BooleanFormula>>.mergeWithAnd(formulaManager: BooleanFormulaManager): BooleanFormula = mergeWith(formulaManager::and, formulaManager)

internal fun Collection<Pair<BooleanFormula, Formula>>.mergeWithOr(formulaManager: BooleanFormulaManager): BooleanFormula = mergeWith(formulaManager::or, formulaManager)

internal fun Map<BooleanFormula, Formula>.mergeWithAnd(formulaManager: BooleanFormulaManager): BooleanFormula = this.entries
    .map { it.key to it.value }
    .mergeWithAnd(formulaManager)

internal fun Map<BooleanFormula, Formula>.mergeWithOr(formulaManager: BooleanFormulaManager): BooleanFormula = this.entries
    .map { it.key to it.value }
    .mergeWithOr(formulaManager)