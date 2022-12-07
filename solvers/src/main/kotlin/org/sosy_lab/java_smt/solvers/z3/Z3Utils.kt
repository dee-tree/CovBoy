package org.sosy_lab.java_smt.solvers.z3

import com.sokolov.covboy.solvers.provers.ExtProverEnvironment
import com.sokolov.covboy.solvers.provers.Prover
import org.sosy_lab.java_smt.api.ProverEnvironment
import java.lang.invoke.MethodHandles

internal fun <T> ExtProverEnvironment.get(getter: Z3AbstractProver<*>.() -> T): T {
    return when (this) {
        is Prover -> delegate.get(getter)
        is Z3Prover -> delegate.get(getter)
        else -> error("Unexpected solver: ${this::class}")
    }
}

internal fun <T> ProverEnvironment.get(getter: Z3AbstractProver<*>.() -> T): T {
    return when (this) {
        is Z3Prover -> delegate.get(getter)
        is Prover -> delegate.get(getter)
        is Z3AbstractProver<*> -> getter()
        else -> error("Unexpected solver: ${this::class}")
    }
}

internal fun ProverEnvironment.z3Context(): Long = get { this.z3context }

internal fun ProverEnvironment.z3Solver(): Long = get { z3solver }

internal fun ProverEnvironment.z3FormulaCreator(): Z3FormulaCreator = get { creator }

internal fun ProverEnvironment.z3FormulaManager(): Z3FormulaManager = get { z3FormulaManagerField.get(this) as Z3FormulaManager }

private val z3FormulaManagerField = MethodHandles
    .privateLookupIn(Z3AbstractProver::class.java, MethodHandles.lookup())
    .findVarHandle(Z3AbstractProver::class.java, "mgr", Z3FormulaManager::class.java)
