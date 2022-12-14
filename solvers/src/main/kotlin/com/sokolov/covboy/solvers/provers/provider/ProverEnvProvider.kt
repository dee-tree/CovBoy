package com.sokolov.covboy.solvers.provers.provider

import org.sosy_lab.java_smt.api.ProverEnvironment
import org.sosy_lab.java_smt.api.SolverContext

fun makeProverEnvironment(context: SolverContext): ProverEnvironment = context.newProverEnvironment(
    SolverContext.ProverOptions.GENERATE_MODELS,
    SolverContext.ProverOptions.GENERATE_UNSAT_CORE
)