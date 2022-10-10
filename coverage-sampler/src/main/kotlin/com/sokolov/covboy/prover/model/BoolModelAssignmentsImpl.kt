package com.sokolov.covboy.prover.model

import com.sokolov.covboy.prover.IProver
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.Model

class BoolModelAssignmentsImpl : AbstractModelAssignments<BooleanFormula>, BoolModelAssignments {

    constructor(model: Model) : super(model)

    constructor(model: Model, expressions: Collection<BooleanFormula>, prover: IProver) : super(model, expressions, prover)

    override fun evaluate(expr: BooleanFormula): BooleanFormula? {
        return super.evaluate(expr)
    }
}