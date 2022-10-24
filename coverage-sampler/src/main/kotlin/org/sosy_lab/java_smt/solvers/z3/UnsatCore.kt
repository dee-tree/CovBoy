package org.sosy_lab.java_smt.solvers.z3

import com.microsoft.z3.Native
import com.sokolov.covboy.prover.BaseProverEnvironment
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.ProverEnvironment


internal fun <T> ProverEnvironment.get(getter: Z3AbstractProver<*>.() -> T): T {
    return when (this) {
        is BaseProverEnvironment -> delegate.get(getter)
        is Z3AbstractProver<*> -> getter()
        else -> error("Unexpected solver: ${this::class}")
    }
}

internal fun ProverEnvironment.z3Context(): Long = get { z3context }

internal fun ProverEnvironment.z3Solver(): Long = get { z3solver }

internal fun ProverEnvironment.z3FormulaCreator(): Z3FormulaCreator = get { creator }

fun BaseProverEnvironment.Z3UnsatCore(): List<BooleanFormula> {

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