package org.sosy_lab.java_smt.basicimpl

import java.lang.invoke.MethodHandles


private val bmgrField = MethodHandles
    .privateLookupIn(AbstractBitvectorFormulaManager::class.java, MethodHandles.lookup())
    .findVarHandle(AbstractBitvectorFormulaManager::class.java, "bmgr", AbstractBooleanFormulaManager::class.java)

internal fun <TFormulaInfo, TType, TEnv, TFuncDecl> AbstractBitvectorFormulaManager<TFormulaInfo, TType, TEnv, TFuncDecl>.bmgr(): AbstractBooleanFormulaManager<TFormulaInfo, TType, TEnv, TFuncDecl> {
    return bmgrField.get(this) as AbstractBooleanFormulaManager<TFormulaInfo, TType, TEnv, TFuncDecl>
}

private val formulaCreatorField = MethodHandles
    .privateLookupIn(AbstractBitvectorFormulaManager::class.java, MethodHandles.lookup())
    .findVarHandle(AbstractBitvectorFormulaManager::class.java, "formulaCreator", FormulaCreator::class.java)

internal fun <TFormulaInfo, TType, TEnv, TFuncDecl> AbstractBitvectorFormulaManager<TFormulaInfo, TType, TEnv, TFuncDecl>.formulaCreator(): FormulaCreator<TFormulaInfo, TType, TEnv, TFuncDecl> {
    return formulaCreatorField.get(this) as FormulaCreator<TFormulaInfo, TType, TEnv, TFuncDecl>
}