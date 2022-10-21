package com.sokolov.covboy.prover

import org.sosy_lab.java_smt.api.*
import java.io.File

open class Prover(
    delegate: ProverEnvironment,
    context: SolverContext,
    formulas: Collection<BooleanFormula>,
) : BaseProverEnvironment(delegate, context) {

    init {
        formulas.forEach(::addConstraint)
    }

    constructor(
        delegate: ProverEnvironment,
        context: SolverContext,
        formulaInputFile: File,
    ) : this(delegate, context, context.formulaManager.readFormulasFromSmtLib(formulaInputFile))


    fun addConstraintsFromSmtLib(input: File): List<BooleanFormula> {
        return fm.readFormulasFromSmtLib(input).onEach(::addConstraint)
    }

    override fun toString(): String = "Prover($solverName)"

}