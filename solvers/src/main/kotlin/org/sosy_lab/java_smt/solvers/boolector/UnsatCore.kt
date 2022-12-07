package org.sosy_lab.java_smt.solvers.boolector

import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.ProverEnvironment
import sun.misc.Unsafe
import java.lang.reflect.Field
import java.lang.reflect.Method


fun ProverEnvironment.boolectorUnsatCore(): List<BooleanFormula> {
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