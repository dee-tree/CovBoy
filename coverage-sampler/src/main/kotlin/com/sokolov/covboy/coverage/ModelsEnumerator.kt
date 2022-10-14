package com.sokolov.covboy.coverage

import com.sokolov.covboy.prover.Assignment
import com.sokolov.covboy.prover.BaseProverEnvironment
import com.sokolov.covboy.prover.Status
import com.sokolov.covboy.prover.model.BoolModelAssignmentsImpl
import com.sokolov.covboy.prover.model.ModelAssignments
import com.sokolov.covboy.smt.isCertainBool
import com.sokolov.covboy.smt.isFalse
import com.sokolov.covboy.smt.isTrue
import com.sokolov.covboy.smt.not
import org.sosy_lab.java_smt.api.*

class ModelsEnumerator(
    private val prover: BaseProverEnvironment,
    private val formulaManager: FormulaManager
) {
    private lateinit var current: Model
    private lateinit var currentModel: ModelAssignments<*>

    private val predicates = prover.booleans

    var traversedModelsCount = 0
        private set

    fun hasNext(): Boolean = prover.check() == Status.SAT


    fun nextModel(exprs: Collection<BooleanFormula>, onModel: (ModelAssignments<BooleanFormula>) -> Unit) {
        current = prover.model

        currentModel = BoolModelAssignmentsImpl(current, exprs, prover)

        // get incomplete models to avoid "unknown"/"undefined" predicates
        val currentConstraints = predicates
            .map { it to (currentModel as BoolModelAssignmentsImpl).evaluate(it) } // TODO: keep in mind that model's eval must be incomplete producer
            .mapNotNull { if (it.second == null) null else Assignment(it.first, it.second!!) }
            .filter { it.value.isCertainBool(formulaManager.booleanFormulaManager) }
            .mergeWithAnd(formulaManager.booleanFormulaManager)

        onModel(currentModel as BoolModelAssignmentsImpl)

        prover.addConstraint(currentConstraints.not(formulaManager.booleanFormulaManager), "concrete-modelneg")
        traversedModelsCount++
    }

    fun take(exprs: Collection<BooleanFormula>, count: Int): List<ModelAssignments<BooleanFormula>> = buildList {
        if (count < 1) return@buildList

        repeat(count) {
            if (!hasNext())
                return@buildList
            nextModel(exprs) { add(it) }
        }
    }


    fun forEach(exprs: Collection<BooleanFormula>, action: (ModelAssignments<BooleanFormula>) -> Unit) {
        while (hasNext())
            nextModel(exprs, action)
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


internal fun Collection<Pair<BooleanFormula, Formula>>.mergeWithAnd(formulaManager: BooleanFormulaManager): BooleanFormula =
    mergeWith(formulaManager::and, formulaManager)

@JvmName("mergeWithAndAssignmentOfBooleanFormula")
internal fun Collection<Assignment<BooleanFormula>>.mergeWithAnd(formulaManager: BooleanFormulaManager): BooleanFormula =
    mergeWith(formulaManager::and, formulaManager)

internal fun Collection<Pair<BooleanFormula, Formula>>.mergeWithOr(formulaManager: BooleanFormulaManager): BooleanFormula =
    mergeWith(formulaManager::or, formulaManager)

internal fun Map<BooleanFormula, Formula>.mergeWithAnd(formulaManager: BooleanFormulaManager): BooleanFormula =
    this.entries
        .map { it.key to it.value }
        .mergeWithAnd(formulaManager)

internal fun Map<BooleanFormula, Formula>.mergeWithOr(formulaManager: BooleanFormulaManager): BooleanFormula =
    this.entries
        .map { it.key to it.value }
        .mergeWithOr(formulaManager)