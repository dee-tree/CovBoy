package com.sokolov.covboy.prover

import org.sosy_lab.common.ShutdownManager
import org.sosy_lab.java_smt.api.*
import java.io.File

open class Prover(
    delegate: ProverEnvironment,
    context: SolverContext,
    private val shutdownManager: ShutdownManager,
    formulas: Collection<BooleanFormula>,
    assertionStorage: AssertionsStorage = AssertionsStorage(delegate, context.formulaManager)
) : BaseProverEnvironment(delegate, context, assertionStorage) {

    init {
        formulas.forEach(::addConstraint)
    }

    constructor(
        delegate: ProverEnvironment,
        context: SolverContext,
        formulaInputFile: File,
        shutdownManager: ShutdownManager
    ) : this(delegate, context, shutdownManager, context.formulaManager.readFormulasFromSmtLib(formulaInputFile))


    fun addConstraintsFromSmtLib(input: File): List<BooleanFormula> {
        return fm.readFormulasFromSmtLib(input).onEach(::addConstraint).also { needCheck() }
    }

    override fun toString(): String = "Prover($solverName)"

}