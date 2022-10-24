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
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.Formula
import org.sosy_lab.java_smt.api.Model

class ModelsEnumerator(
    private val prover: BaseProverEnvironment,
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

        val currentConstraints = predicates
            .map { it to (currentModel as BoolModelAssignmentsImpl).evaluate(it) }
            .mapNotNull { if (it.second == null) null else Assignment(it.first, it.second!!) }
            .filter { it.value.isCertainBool(prover.fm.booleanFormulaManager) }
            .mergeWithAnd(prover)

        onModel(currentModel as BoolModelAssignmentsImpl)

        prover.addConstraint(currentConstraints.not(prover), false,"concrete-modelneg")
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
    prover: BaseProverEnvironment
): BooleanFormula {
    if (size == 1) return first().let {
        when {
            it.second.isFalse(prover.fm.booleanFormulaManager) -> it.first.not(prover)
            else -> it.first
        }
    }

    return merger(
        (filter { it.second.isTrue(prover.fm.booleanFormulaManager) }.map { it.first }
                + filter { it.second.isFalse(prover.fm.booleanFormulaManager) }.map { it.first.not(prover) }
                ).toTypedArray()
    )
}

@JvmName("mergeWithAssignmentOfBooleanFormula")
internal fun Collection<Assignment<BooleanFormula>>.mergeWith(
    merger: (Array<out BooleanFormula>) -> BooleanFormula,
    prover: BaseProverEnvironment
): BooleanFormula {
    if (size == 1) return first().let {
        when {
            it.value.isFalse(prover.fm.booleanFormulaManager) -> it.expr.not(prover)
            else -> it.expr
        }
    }

    return merger(
        (filter { it.value.isTrue(prover.fm.booleanFormulaManager) }.map { it.expr }
                + filter { it.value.isFalse(prover.fm.booleanFormulaManager) }.map { it.expr.not(prover) }
                ).toTypedArray()
    )
}


internal fun Collection<Pair<BooleanFormula, Formula>>.mergeWithAnd(prover: BaseProverEnvironment): BooleanFormula =
    mergeWith(prover.fm.booleanFormulaManager::and, prover)

@JvmName("mergeWithAndAssignmentOfBooleanFormula")
internal fun Collection<Assignment<BooleanFormula>>.mergeWithAnd(prover: BaseProverEnvironment): BooleanFormula =
    mergeWith(prover.fm.booleanFormulaManager::and, prover)

internal fun Collection<Pair<BooleanFormula, Formula>>.mergeWithOr(prover: BaseProverEnvironment): BooleanFormula =
    mergeWith(prover.fm.booleanFormulaManager::or, prover)

internal fun Map<BooleanFormula, Formula>.mergeWithAnd(prover: BaseProverEnvironment): BooleanFormula =
    this.entries
        .map { it.key to it.value }
        .mergeWithAnd(prover)

internal fun Map<BooleanFormula, Formula>.mergeWithOr(prover: BaseProverEnvironment): BooleanFormula =
    this.entries
        .map { it.key to it.value }
        .mergeWithOr(prover)