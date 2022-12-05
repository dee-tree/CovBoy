package org.sosy_lab.java_smt.solvers.boolector

import org.sosy_lab.common.NativeLibraries
import org.sosy_lab.common.ShutdownNotifier
import org.sosy_lab.common.configuration.Configuration
import org.sosy_lab.common.io.PathCounterTemplate
import org.sosy_lab.common.log.LogManager
import java.lang.invoke.MethodHandles
import java.util.function.Consumer

private val setOptionsMethod = MethodHandles.lookup().unreflect(
    BoolectorSolverContext::class.java.getDeclaredMethod(
        "setOptions",
        Configuration::class.java,
        PathCounterTemplate::class.java,
        Long::class.javaPrimitiveType,
        Long::class.javaPrimitiveType
    )
        .also { it.isAccessible = true }
)

fun createExtendedBoolectorSolverContext(
    configuration: Configuration = Configuration.defaultConfiguration(),
    logManager: LogManager = LogManager.createNullLogManager(),
    shutdownNotifier: ShutdownNotifier = ShutdownNotifier.createDummy(),
    loader: (String) -> Unit = NativeLibraries::loadLibrary
): BoolectorSolverContext {
    return getExtendedBoolectorSolverContext(
        configuration,
        shutdownNotifier,
        null,
        42,
        loader
    )
}

fun getExtendedBoolectorSolverContext(
    config: Configuration,
    pShutdownNotifier: ShutdownNotifier,
    solverLogfile: PathCounterTemplate?,
    randomSeed: Long,
    pLoader: Consumer<String>
): BoolectorSolverContext {
    pLoader.accept("boolector")

    val btor = BtorJNI.boolector_new()

    setOptionsMethod.invoke(config, solverLogfile, randomSeed, btor)

    val creator = ExtendedBoolectorFormulaCreator(BoolectorFormulaCreator(btor))
    val functionTheory = BoolectorUFManager(creator)
    val booleanTheory = ExtendedBoolectorBooleanFormulaManager(
        BoolectorBooleanFormulaManager(creator), creator
    )

    val bitvectorTheory = ExtendedBoolectorBitvectorFormulaManager(
        BoolectorBitvectorFormulaManager(creator, booleanTheory),
        booleanTheory,
        creator
    )

    val quantifierTheory = BoolectorQuantifiedFormulaManager(creator)
    val arrayTheory = BoolectorArrayFormulaManager(creator)
    val manager = BoolectorFormulaManager(
        creator, functionTheory, booleanTheory, bitvectorTheory, quantifierTheory, arrayTheory
    )
    return BoolectorSolverContext(manager, creator, pShutdownNotifier);
}
