package com.sokolov.covboy.prover.model

import com.sokolov.covboy.prover.Assignment
import com.sokolov.covboy.prover.IProver
import org.sosy_lab.java_smt.api.Formula
import org.sosy_lab.java_smt.api.FormulaManager
import org.sosy_lab.java_smt.api.Model
import java.math.BigInteger

abstract class AbstractModelAssignments<T : Formula> private constructor(
    assignments: Collection<Assignment<Formula>>
) : ModelAssignments<T> {

    protected val assignments = assignments.toList()

    constructor(model: Model) : this(model.map { Assignment(it.key, it.valueAsFormula) }) {
    }

    constructor(model: Model, expressions: Collection<T>, prover: IProver) : this(expressions.mapNotNull {
        try { model.evaluate(it)
        } catch (e: Exception) {
            null
        }?.let { value -> Assignment(it, value.toFormula(prover.context.formulaManager)) } ?: kotlin.run {
            null
        }
    })

    override fun evaluate(expr: T): T? {
        return assignments.find { it.expr == expr }?.value as? T
    }

}

private fun Any.toFormula(fm: FormulaManager): Formula = when (this) {
    is Boolean -> fm.booleanFormulaManager.makeBoolean(this)
    is BigInteger -> fm.integerFormulaManager.makeNumber(this)
    else -> error("Unable to translate $this to Formula currently :-0 (because I was so lazy and it was enough to deal with booleans)")
}