package org.sosy_lab.java_smt.basicimpl

import org.sosy_lab.java_smt.api.Formula
import java.lang.invoke.MethodHandles

fun Formula.isAbstractFormula(): Boolean {
    return this is AbstractFormula<*>
}

internal fun <T> Formula.get(getter: AbstractFormula<*>.() -> T): T {
    return when (this) {
        is AbstractFormula<*> -> getter()
        else -> error("Unexpected formula: ${this::class}")
    }
}

internal fun Formula.formulaInfo(): Any {
    return get { abstractFormulaInfo.get(this) }
}


private val abstractFormulaInfo = MethodHandles
    .privateLookupIn(AbstractFormula::class.java, MethodHandles.lookup())
    .findVarHandle(AbstractFormula::class.java, "formulaInfo", Any::class.java)
