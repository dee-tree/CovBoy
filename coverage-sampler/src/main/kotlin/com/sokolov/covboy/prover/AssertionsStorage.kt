package com.sokolov.covboy.prover

import com.sokolov.covboy.prover.assertions.AssertionListener
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.FormulaManager
import org.sosy_lab.java_smt.api.ProverEnvironment

class AssertionsStorage(
    private val prover: ProverEnvironment,
    private val fm: FormulaManager,
    var assertionListener: AssertionListener? = null,
    vararg initial: Assertion,
) {
    private val storage = mutableSetOf<Assertion>(*initial)

    val size: Int
        get() = storage.size

    val assumptions: List<BooleanFormula>
        get() = storage.mapNotNull { if (it.enabled) it.assumption else null }

    private fun assertSafely(expr: BooleanFormula, tag: String): Assertion {
        val assertion = Assertion(fm, expr, tag, object : AssertionListener {
            override fun onAssertionEnabled(assertion: Assertion) { assertionListener?.onAssertionEnabled(assertion) }
            override fun onAssertionDisabled(assertion: Assertion) { assertionListener?.onAssertionDisabled(assertion) }
        })

        storage.find { it.uid == assertion.uid }?.let { it.enable(); return it }
        return assertion.put(prover).also { storage.add(it) }
    }

    fun assert(expr: BooleanFormula, tag: String): Assertion {
        return assertSafely(expr, tag)
    }

    fun forEach(action: (Assertion) -> Unit) {
        storage.forEach(action)
    }

    val assertions: Set<Assertion>
        get() = storage

    fun getById(uid: String): Assertion = storage.first { it.uid == uid }
    fun getByTag(tag: String): List<Assertion> = storage.filter { it.tag == tag }
    fun getByTag(onTag: (String) -> Boolean): List<Assertion> = storage.filter { onTag(it.tag) }

    fun enabled(uid: String): Boolean = storage.first { it.uid == uid }.enabled
}