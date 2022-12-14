package com.sokolov.covboy.coverage

import com.sokolov.covboy.solvers.formulas.asNonSwitchableConstraint
import com.sokolov.covboy.solvers.formulas.utils.asFormula
import com.sokolov.covboy.solvers.formulas.utils.notOptimized
import com.sokolov.covboy.solvers.provers.Prover
import com.sokolov.covboy.solvers.provers.Status
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.Model

class ModelsEnumerator(
    private val prover: Prover,
) {
    private lateinit var currentModel: List<Model.ValueAssignment>

    private val predicates = prover.booleans

    var traversedModelsCount = 0
        private set

    fun hasNext(): Boolean = prover.checkSat() == Status.SAT


    fun nextModel(exprs: Collection<BooleanFormula>, onModel: (List<Model.ValueAssignment>) -> Unit) {
        currentModel = prover.modelAssignments

        println("Predicates: $predicates")
        val currentConstraints = predicates
            .mapNotNull { predicate -> currentModel.find { predicate == it.key } }
            .map { it.asFormula(prover.fm) }
            .let { prover.fm.booleanFormulaManager.and(it) }

        onModel(currentModel.filter { it.key in exprs })

        val negatedModel = prover.fm.booleanFormulaManager
            .notOptimized(currentConstraints)
            .asNonSwitchableConstraint(prover.fm)

        prover.addConstraint(negatedModel)
        traversedModelsCount++
    }

    fun take(exprs: Collection<BooleanFormula>, count: Int): List<List<Model.ValueAssignment>> = buildList {
        if (count < 1) return@buildList

        repeat(count) {
            if (!hasNext())
                return@buildList
            nextModel(exprs) { add(it) }
        }
    }


    fun forEach(exprs: Collection<BooleanFormula>, action: (List<Model.ValueAssignment>) -> Unit) {
        while (hasNext())
            nextModel(exprs, action)
    }

}
