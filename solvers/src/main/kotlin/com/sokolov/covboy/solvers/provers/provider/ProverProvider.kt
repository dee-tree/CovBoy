package com.sokolov.covboy.solvers.provers.provider

import com.sokolov.covboy.solvers.provers.Prover
import com.sokolov.covboy.solvers.provers.secondary.SecondaryProver
import org.sosy_lab.java_smt.SolverContextFactory.Solvers

var PRIMARY_SOLVER = Solvers.Z3


fun makeProver(primary: Boolean, solver: Solvers): Prover {
    val ctx = makeContext(solver)
    val proverEnv = makeProverEnvironment(ctx)

    return if (primary) {
        Prover(proverEnv, ctx)
    } else {
        val primaryProver = makeProver(true, PRIMARY_SOLVER)
        SecondaryProver(proverEnv, ctx, primaryProver)
    }
}

fun makePrimaryProver(solver: Solvers = PRIMARY_SOLVER) = makeProver(true, solver)

fun makeProver(solver: Solvers): Prover = if (solver == PRIMARY_SOLVER)
    makePrimaryProver()
else makeProver(false, solver)