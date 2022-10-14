package org.sosy_lab.java_smt.solvers.boolector

import com.sokolov.covboy.prover.BaseProverEnvironment
import com.sokolov.covboy.prover.Prover
import com.sokolov.covboy.prover.SecondaryProver
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.ProverEnvironment
import sun.misc.Unsafe
import java.lang.reflect.Field

private fun ProverEnvironment.btor(): Long {
    val proverEnv: BoolectorAbstractProver<*>
    when {
        this is SecondaryProver -> {
            val delegateField = this::class.java.getDeclaredField("delegate")
            delegateField.isAccessible = true
            proverEnv = delegateField.get(this) as BoolectorAbstractProver<*>
        }
        else -> { proverEnv = this as BoolectorAbstractProver<*> }
    }

    val btorField = proverEnv::class.java.getDeclaredFieldRecursively("btor")
    btorField.isAccessible = true

    return btorField.getLong(proverEnv)
}

private fun ProverEnvironment.boolectorFormulaCreator(): BoolectorFormulaCreator {
    val proverEnv: BoolectorAbstractProver<*>
    when {
        this is SecondaryProver -> {
            val delegateField = this::class.java.getDeclaredField("delegate")
            delegateField.isAccessible = true
            proverEnv = delegateField.get(this) as BoolectorAbstractProver<*>
        }
        else -> { proverEnv = this as BoolectorAbstractProver<*> }
    }

    val creatorField = proverEnv::class.java.getDeclaredFieldRecursively("creator")
    creatorField.isAccessible = true

    return creatorField.get(proverEnv) as BoolectorFormulaCreator
}

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


fun Prover.BoolectorUnsatCoreWithAssumptions(): List<BooleanFormula> {
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
