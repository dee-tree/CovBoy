package org.sosy_lab.java_smt.solvers.princess

import org.sosy_lab.java_smt.api.ProverEnvironment
import org.sosy_lab.java_smt.basicimpl.withAssumptionsWrapper.BasicProverWithAssumptionsWrapper
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

private val clearAssumptionsMethod = MethodHandles.lookup().unreflect(
    BasicProverWithAssumptionsWrapper::class.java.getDeclaredMethod("clearAssumptions")
        .also { it.isAccessible = true }
)


fun ProverEnvironment.princessClearAssumptions() {
    clearAssumptionsMethod.invoke(this as BasicProverWithAssumptionsWrapper<*, *>)
}