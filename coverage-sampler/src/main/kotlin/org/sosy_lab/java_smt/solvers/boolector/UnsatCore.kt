package org.sosy_lab.java_smt.solvers.boolector

import com.sokolov.covboy.prover.BaseProverEnvironment
import com.sokolov.covboy.prover.Prover
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.ProverEnvironment
import sun.misc.Unsafe
import java.lang.invoke.MethodHandles
import java.lang.reflect.Field
import java.lang.reflect.Method


/**
 * boolector context
 */
private val btorField = MethodHandles
    .privateLookupIn(BoolectorAbstractProver::class.java, MethodHandles.lookup())
    .findVarHandle(BoolectorAbstractProver::class.java, "btor", Long::class.javaPrimitiveType)

private val boolectorFormulaCreator = MethodHandles
    .privateLookupIn(BoolectorAbstractProver::class.java, MethodHandles.lookup())
    .findVarHandle(BoolectorAbstractProver::class.java, "creator", BoolectorFormulaCreator::class.java)

internal fun <T> ProverEnvironment.get(getter: BoolectorAbstractProver<*>.() -> T): T {
    return when (this) {
        is BaseProverEnvironment -> delegate.get(getter)
        is BoolectorAbstractProver<*> -> getter()
        else -> error("Unexpected solver: ${this::class}")
    }
}

internal fun ProverEnvironment.btor(): Long {
    return get { btorField.get(this) as Long }
}

internal fun ProverEnvironment.boolectorFormulaCreator(): BoolectorFormulaCreator = get { boolectorFormulaCreator.get(this) as BoolectorFormulaCreator }

fun BaseProverEnvironment.boolectorUnsatCoreWithAssumptions(): List<BooleanFormula> {
    val f = Unsafe::class.java.getDeclaredField("theUnsafe")
    f.isAccessible = true
    val unsafe = f.get(null) as Unsafe

    return buildList {
        var currentPointer = BtorJNI.boolector_get_failed_assumptions(btor())
        var nodePointer = unsafe.getLong(currentPointer);

        while (nodePointer != 0L) {
            add(boolectorFormulaCreator().encapsulateBoolean(nodePointer))

            currentPointer += 8 // Next item of array
            nodePointer = unsafe.getLong(currentPointer)
        }

    }

}

fun Class<*>.getDeclaredFieldRecursively(name: String): Field {
    var current: Class<*>? = this
    while (current != null) {
        try {
            return current.getDeclaredField(name)
        } catch (_: Throwable) {
            5 + 3  // P R I K O L
        }
        current = current.superclass
    }

    throw NoSuchFieldException()
}

fun Class<*>.getDeclaredMethodRecursively(name: String, vararg args: Class<*>): Method {
    var current: Class<*>? = this
    while (current != null) {
        try {
            return current.getDeclaredMethod(name, *args)
        } catch (_: Throwable) { }
        current = current.superclass
    }

    throw NoSuchFieldException()
}