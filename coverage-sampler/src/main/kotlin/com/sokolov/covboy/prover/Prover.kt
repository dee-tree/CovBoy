package com.sokolov.covboy.prover

import org.sosy_lab.java_smt.api.*
import org.sosy_lab.java_smt.solvers.z3.z3Assertions
import org.sosy_lab.java_smt.solvers.z3.z3FromFile
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
    ) : this(delegate, context, emptyList()) {
        readFromFile(formulaInputFile)
    }

    override fun toString(): String = "Prover($solverName)"

}