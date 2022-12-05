package com.sokolov.covboy.solvers.provers.wrap

import com.sokolov.covboy.solvers.provers.secondary.ConstraintStoredProver
import org.sosy_lab.java_smt.SolverContextFactory.Solvers
import org.sosy_lab.java_smt.api.ProverEnvironment
import org.sosy_lab.java_smt.solvers.boolector.BoolectorConstraintStoredProver
import org.sosy_lab.java_smt.solvers.boolector.BoolectorProver
import org.sosy_lab.java_smt.solvers.smtinterpol.SmtInterpolConstraintStoredProver
import org.sosy_lab.java_smt.solvers.smtinterpol.SmtInterpolProver
import org.sosy_lab.java_smt.solvers.z3.Z3ConstraintStoredProver
import org.sosy_lab.java_smt.solvers.z3.Z3Prover

fun ProverEnvironment.wrap(solver: Solvers): ConstraintStoredProver = when (solver) {
    Solvers.Z3 -> Z3ConstraintStoredProver(Z3Prover.wrap(this))
    Solvers.BOOLECTOR -> BoolectorConstraintStoredProver(BoolectorProver.wrap(this))
    Solvers.SMTINTERPOL -> SmtInterpolConstraintStoredProver(SmtInterpolProver.wrap(this))
    else -> TODO("make wrapper for solver: $solver")
}