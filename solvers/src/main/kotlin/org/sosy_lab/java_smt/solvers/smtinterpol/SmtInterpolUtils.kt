package org.sosy_lab.java_smt.solvers.smtinterpol

import com.google.common.collect.ImmutableMap
import com.sokolov.covboy.solvers.provers.ExtProverEnvironment
import com.sokolov.covboy.solvers.provers.Prover
import de.uni_freiburg.informatik.ultimate.logic.Script
import de.uni_freiburg.informatik.ultimate.logic.Term
import org.sosy_lab.java_smt.api.Formula
import org.sosy_lab.java_smt.api.ProverEnvironment
import org.sosy_lab.java_smt.api.SolverContext
import org.sosy_lab.java_smt.basicimpl.formulaInfo
import org.sosy_lab.java_smt.basicimpl.get
import org.sosy_lab.java_smt.basicimpl.withAssumptionsWrapper.ProverWithAssumptionsWrapper
import org.sosy_lab.java_smt.basicimpl.withAssumptionsWrapper.basicProverWithAssumptionsDelegate
import java.lang.invoke.MethodHandles
import java.lang.invoke.VarHandle
import javax.annotation.concurrent.Immutable

internal fun <T> ExtProverEnvironment.get(getter: SmtInterpolAbstractProver<*, *>.() -> T): T {
    return when (this) {
        is Prover -> delegate.get(getter)
        is SmtInterpolProver -> delegate.get(getter)
        else -> error("Unexpected solver: ${this::class}")
    }
}

internal fun <T> ProverEnvironment.get(getter: SmtInterpolAbstractProver<*, *>.() -> T): T {
    return when (this) {
        is ProverWithAssumptionsWrapper -> (this.basicProverWithAssumptionsDelegate() as SmtInterpolTheoremProver).getter()
        is SmtInterpolProver -> delegate.get(getter)
        is Prover -> delegate.get(getter)
        is SmtInterpolAbstractProver<*, *> -> getter()
        else -> error("Unexpected solver: ${this::class}")
    }
}

internal fun <T> SolverContext.get(getter: SmtInterpolSolverContext.() -> T): T {
    return when (this) {
        is SmtInterpolSolverContext -> getter()
        else -> error("Unexpected context: ${this::class}")
    }
}

private fun SolverContext.smtInterpolSettings(): Any = get { smtInterpolContextSettingsField.get(this) }

internal fun SolverContext.smtInterpolOptions(): Map<String, Any> = smtInterpolSettingsOptionMapField.get(smtInterpolSettings()) as Map<String, Any>

internal fun SolverContext.smtInterpolAddOption(option: String, value: Any) {
    val options = smtInterpolSettingsOptionMapField.get(smtInterpolSettings()) as Map<String, Any>
    val newOptions = ImmutableMap.copyOf(options.toMutableMap().apply { put(option, value) })

    val optionsMapField = SmtInterpolSettingsClass.getDeclaredField("optionsMap")
    optionsMapField.trySetAccessible()
    optionsMapField.set(smtInterpolSettings(), newOptions)
}

internal fun ProverEnvironment.smtInterpolScript(): Script = get { smtInterpolScriptField.get(this) as Script }

internal fun ProverEnvironment.smtInterpolFormulaManager(): SmtInterpolFormulaManager = get {
    smtInterpolFormulaManagerField.get(this) as SmtInterpolFormulaManager
}

internal fun Formula.smtInterpolTerm(): Term = this.formulaInfo() as Term

private val smtInterpolScriptField = MethodHandles
    .privateLookupIn(SmtInterpolAbstractProver::class.java, MethodHandles.lookup())
    .findVarHandle(SmtInterpolAbstractProver::class.java, "env", Script::class.java)

private val smtInterpolFormulaManagerField = MethodHandles
    .privateLookupIn(SmtInterpolAbstractProver::class.java, MethodHandles.lookup())
    .findVarHandle(SmtInterpolAbstractProver::class.java, "mgr", SmtInterpolFormulaManager::class.java)

private val SmtInterpolSettingsClass = Class.forName("org.sosy_lab.java_smt.solvers.smtinterpol.SmtInterpolSolverContext\$SmtInterpolSettings")

private val smtInterpolContextSettingsField = MethodHandles
    .privateLookupIn(SmtInterpolSolverContext::class.java, MethodHandles.lookup())
    .findVarHandle(SmtInterpolSolverContext::class.java, "settings", SmtInterpolSettingsClass)

private val smtInterpolSettingsOptionMapField = MethodHandles
    .privateLookupIn(SmtInterpolSettingsClass, MethodHandles.lookup())
    .findVarHandle(SmtInterpolSettingsClass, "optionsMap", ImmutableMap::class.java)