package com.sokolov.covboy.solvers.provers.provider

import org.sosy_lab.java_smt.SolverContextFactory
import org.sosy_lab.java_smt.api.SolverContext
import org.sosy_lab.java_smt.solvers.smtinterpol.SmtInterpolSolverContext
import org.sosy_lab.java_smt.solvers.smtinterpol.smtInterpolAddOption

fun makeContext(solver: SolverContextFactory.Solvers): SolverContext = SolverContextFactory.createSolverContext(solver).also {
    if (solver == SolverContextFactory.Solvers.SMTINTERPOL) {
        (it as SmtInterpolSolverContext).smtInterpolAddOption(":produce-unsat-assumptions", true)
    }
}