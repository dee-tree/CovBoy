package org.sosy_lab.java_smt.solvers.boolector

import com.sokolov.covboy.solvers.provers.ExtProverEnvironment
import com.sokolov.covboy.solvers.provers.Prover
import org.sosy_lab.java_smt.api.ProverEnvironment
import java.lang.invoke.MethodHandles

internal fun <T> ExtProverEnvironment.get(getter: BoolectorAbstractProver<*>.() -> T): T {
    return when (this) {
        is Prover -> delegate.get(getter)
        is BoolectorProver -> delegate.get(getter)
        else -> error("Unexpected solver: ${this::class}")
    }
}

internal fun <T> ProverEnvironment.get(getter: BoolectorAbstractProver<*>.() -> T): T {
    return when (this) {
        is BoolectorProver -> delegate.get(getter)
        is Prover -> delegate.get(getter)
        is BoolectorAbstractProver<*> -> getter()
        else -> error("Unexpected solver: ${this::class}")
    }
}


internal fun ProverEnvironment.btor(): Long {
    return get { btorField.get(this) as Long }
}

internal fun ProverEnvironment.boolectorFormulaCreator(): BoolectorFormulaCreator =
    get { boolectorFormulaCreator.get(this) as BoolectorFormulaCreator }

internal fun ProverEnvironment.boolectorFormulaManager(): BoolectorFormulaManager =
    get { boolectorFormulaManagerField.get(this) as BoolectorFormulaManager }

/**
 * boolector context
 */
private val btorField = MethodHandles
    .privateLookupIn(BoolectorAbstractProver::class.java, MethodHandles.lookup())
    .findVarHandle(BoolectorAbstractProver::class.java, "btor", Long::class.javaPrimitiveType)

private val boolectorFormulaCreator = MethodHandles
    .privateLookupIn(BoolectorAbstractProver::class.java, MethodHandles.lookup())
    .findVarHandle(BoolectorAbstractProver::class.java, "creator", BoolectorFormulaCreator::class.java)

private val boolectorFormulaManagerField = MethodHandles
    .privateLookupIn(BoolectorAbstractProver::class.java, MethodHandles.lookup())
    .findVarHandle(BoolectorAbstractProver::class.java, "manager", BoolectorFormulaManager::class.java)
