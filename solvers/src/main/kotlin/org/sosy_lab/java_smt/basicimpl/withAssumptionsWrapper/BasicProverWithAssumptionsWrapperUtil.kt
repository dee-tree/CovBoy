package org.sosy_lab.java_smt.basicimpl.withAssumptionsWrapper

import org.sosy_lab.java_smt.api.BasicProverEnvironment
import org.sosy_lab.java_smt.api.ProverEnvironment
import java.lang.invoke.MethodHandles

private val basicProverWithAssumptionsWrapperFormulaCreatorField = MethodHandles
    .privateLookupIn(BasicProverWithAssumptionsWrapper::class.java, MethodHandles.lookup())
    .findVarHandle(BasicProverWithAssumptionsWrapper::class.java, "delegate", BasicProverEnvironment::class.java)

internal fun ProverEnvironment.basicProverWithAssumptionsDelegate(): BasicProverEnvironment<*> {
    return basicProverWithAssumptionsWrapperFormulaCreatorField.get(this as BasicProverWithAssumptionsWrapper<*, *>) as BasicProverEnvironment<*>
}
