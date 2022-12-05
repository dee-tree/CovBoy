package org.sosy_lab.java_smt.solvers.z3

import com.microsoft.z3.Native
import com.sokolov.covboy.solvers.provers.Prover
import com.sokolov.covboy.solvers.provers.ExtProverEnvironment
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.ProverEnvironment

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

internal fun ProverEnvironment.z3UnsatCore(): List<BooleanFormula> {

    val z3context = this.z3Context()
    val z3solver = this.z3Solver()
    val unsatCore = Native.solverGetUnsatCore(z3context, z3solver)
    Native.astVectorIncRef(z3context, unsatCore)

    return try {
        val size = Native.astVectorSize(z3context, unsatCore)

        List(size) { i ->
            val ast = Native.astVectorGet(z3context, unsatCore, i)
            this.z3FormulaCreator().encapsulateBoolean(ast)
        }

    } finally {
        Native.astVectorDecRef(z3context, unsatCore)
    }
}