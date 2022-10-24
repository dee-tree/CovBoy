package com.sokolov.covboy.prover

import com.sokolov.covboy.logger
import org.sosy_lab.java_smt.SolverContextFactory.Solvers
import org.sosy_lab.java_smt.api.*
import org.sosy_lab.java_smt.solvers.boolector.boolectorUnsatCoreWithAssumptions
import org.sosy_lab.java_smt.solvers.boolector.isBoolectorFormula
import org.sosy_lab.java_smt.solvers.cvc4.isCVC4Formula
import org.sosy_lab.java_smt.solvers.z3.Z3UnsatCore
import org.sosy_lab.java_smt.solvers.z3.addConstraintCustom
import org.sosy_lab.java_smt.solvers.z3.isZ3Formula
import java.util.*
import java.util.concurrent.TimeoutException

abstract class BaseProverEnvironment(
    open val delegate: ProverEnvironment,
    val context: SolverContext,
    val timeoutOnCheck: Long = 60_000L
) : ProverEnvironment by delegate {


    val solverName: Solvers
        get() = context.solverName

    open val fm: FormulaManager
        get() = context.formulaManager

    private val _checkCounter = mutableMapOf<String, MutableChecksCounter>()
    val checkCounter: Map<String, ChecksCounter>
        get() = _checkCounter

    val assumedFormulas: List<BooleanFormula>
        get() = constraints.filter { it.enabled }.map { it.asFormula }

    val formulas: List<BooleanFormula>
        get() = switchableConstraints.filter { it.enabled }.map { it.original } +
                constraints.filterIsInstance<NonSwitchableConstraint>().map { it.asFormula }

    val assumptions: List<BooleanFormula>
        get() = switchableConstraints.filter { it.enabled }.map { it.assumption }

    protected val constraintsStack: Stack<List<Constraint>> = Stack()
    protected val currentLevelConstraints = mutableListOf<Constraint>()

    protected val constraints: List<Constraint>
        get() = constraintsStack.fold(emptyList<Constraint>()) { curr, acc -> acc + curr } + currentLevelConstraints

    protected val switchableConstraints: List<MutableSwitchableConstraint>
        get() = constraints.filterIsInstance<MutableSwitchableConstraint>()


    // checks optimization via last status remember
    private var lastCheckStatus: Status? = null
    private var isCheckNeed: Boolean = true
    private var lastCheckAssumptions: List<BooleanFormula> = emptyList()

    init {
        push()
    }

    fun reset() {
        while (constraintsStack.isNotEmpty()) {
            pop()
        }
    }

    protected fun needCheck() {
        isCheckNeed = true
    }

    val unsatCoreWithAssumptions: List<BooleanFormula>
        get() {

            return when (solverName) {
                Solvers.BOOLECTOR -> boolectorUnsatCoreWithAssumptions()
                Solvers.Z3 -> Z3UnsatCore()
                else -> delegate.unsatCore
            }
        }

    override fun getUnsatCore(): List<BooleanFormula> = unsatCoreWithAssumptions

    override fun unsatCoreOverAssumptions(p0: MutableCollection<BooleanFormula>): Optional<MutableList<BooleanFormula>> {
        error("Use unsatCoreWithAssumptions or unsatCore (they are the same)")
    }

    override fun isUnsatWithAssumptions(p0: MutableCollection<BooleanFormula>): Boolean {
        error("Use isUnsat or check")
    }

    open val booleans: Set<BooleanFormula>
        get() = buildSet {
            formulas.forEach { f -> addAll(f.getDeepestBooleanExprs(context.formulaManager)) }
        }

    fun addConstraint(formula: BooleanFormula, switchable: Boolean, tag: String = ""): BooleanFormula {

        if (formula in formulas) {
            System.err.println("You're trying to add constraint that already added before: $formula")
        }

        val constraint = if (switchable) {
            MutableSwitchableConstraint(formula, tag, enabled = true, fm)
        } else {
            NonSwitchableConstraint(formula)
        }

        delegate.addConstraintCustom(constraint.asFormula)
        currentLevelConstraints.add(constraint)

        needCheck()

        return (constraint as? SwitchableConstraint)?.original ?: constraint.asFormula
    }

    /**
     * add hard constraint without ability to disable it
     */
    override fun addConstraint(f: BooleanFormula): Void? {
        addConstraint(f, false)
        return null
    }

    final override fun isUnsat(): Boolean = delegate.isUnsat

    fun check(reason: String = "common"): Status {
        if (!isCheckNeed && lastCheckAssumptions == assumptions)
            return lastCheckStatus!!

        lastCheckAssumptions = assumptions.toList()

        lastCheckStatus = try {
            println("go to check")
            val isUnsat = delegate.isUnsatWithAssumptions(assumptions)
            println("checked")
            if (isUnsat) Status.UNSAT else Status.SAT
        } catch (e: SolverException) {
            Status.UNKNOWN
        } catch (e: TimeoutException) {
            Status.UNKNOWN
        }

        isCheckNeed = false
        _checkCounter.getOrPut(reason) { MutableChecksCounter() }.update(lastCheckStatus!!)
        return lastCheckStatus!!
    }

    override fun push() {
        logger().trace("push: $currentLevelConstraints")
        constraintsStack.push(currentLevelConstraints.toList())
        currentLevelConstraints.clear()
        delegate.push()
        needCheck()
    }

    override fun pop() {
        logger().trace("pop: ${constraintsStack.peek()}")
        delegate.pop()
        currentLevelConstraints.clear()
        currentLevelConstraints.addAll(constraintsStack.pop())
        needCheck()
    }


    fun disableConstraint(formula: BooleanFormula) {
        switchableConstraints.find { it.original == formula }?.disable()
            ?: error("It's not possible to disable the assertion which is not added already")

        needCheck()
    }

    fun enableConstraint(formula: BooleanFormula) {
        switchableConstraints.find { it.original == formula }?.enable()
            ?: error("It's not possible to enable the assertion which is not added already")

        needCheck()
    }

    val enabledSwitchableConstraints: List<BooleanFormula>
        get() = switchableConstraints.filter { it.enabled }.map { it.original }

    fun filterSwitchableConstraints(filter: (SwitchableConstraint) -> Boolean): List<BooleanFormula> {
        return switchableConstraints.filter(filter).map { it.original }
    }


    fun thisSolverImplFormula(formula: Formula): Boolean {
        return when (solverName) {
            Solvers.Z3 -> formula.isZ3Formula()
            Solvers.CVC4 -> formula.isCVC4Formula()
            Solvers.BOOLECTOR -> formula.isBoolectorFormula()
            else -> error("Unsupported solver $solverName")
        }
    }
}