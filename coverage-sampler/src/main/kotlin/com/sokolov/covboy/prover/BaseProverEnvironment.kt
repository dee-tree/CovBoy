package com.sokolov.covboy.prover

import com.microsoft.z3.Native
import com.sokolov.covboy.logger
import com.sokolov.covboy.smt.isCertainBool
import org.sosy_lab.java_smt.SolverContextFactory.Solvers
import org.sosy_lab.java_smt.api.*
import org.sosy_lab.java_smt.solvers.boolector.boolectorUnsatCoreWithAssumptions
import org.sosy_lab.java_smt.solvers.boolector.isBoolectorFormula
import org.sosy_lab.java_smt.solvers.cvc4.isCVC4Formula
import org.sosy_lab.java_smt.solvers.princess.isPrincessFormula
import org.sosy_lab.java_smt.solvers.smtinterpol.isSmtInterpolFormula
import org.sosy_lab.java_smt.solvers.z3.*
import java.io.File
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

    /*protected */val constraints: List<Constraint>
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

    open fun reset() {
        while (constraintsStack.isNotEmpty()) {
            pop()
        }

        currentLevelConstraints.clear()
        constraintsStack.clear()
        lastCheckAssumptions = emptyList()

        if (solverName == Solvers.Z3) {
            Native.solverReset(z3Context(), z3Solver())
        }
    }

    protected fun needCheck() {
        isCheckNeed = true
    }

    val unsatCoreWithAssumptions: List<BooleanFormula>
        get() {

            val uc = when (solverName) {
                Solvers.BOOLECTOR -> boolectorUnsatCoreWithAssumptions()
                Solvers.Z3 -> Z3UnsatCore()
                else -> delegate.unsatCore
            }

            /*
            fix to get only assumptions from unsat core, because sometimes it can return full expression,
            which was assumed (assumption -> expr. We need - assumption, but can get "assumption -> expr").
            Reproduced in: Princess
            */
            return uc.map { ucExpr ->
                val enabledSwConstraints = switchableConstraints.filter { it.enabled }
                enabledSwConstraints.find { /*java smt unsat core (princess, smtinterpol, cvc4)*/it.asFormula == ucExpr || it.assumption == ucExpr /*Z3 returns assumption*/ }?.original
                    ?: ucExpr
            }.toSet().toList()
        }

    override fun getUnsatCore(): List<BooleanFormula> = unsatCoreWithAssumptions

    override fun unsatCoreOverAssumptions(p0: MutableCollection<BooleanFormula>): Optional<MutableList<BooleanFormula>> {
        error("Use unsatCoreWithAssumptions or unsatCore (they are the same)")
    }

    override fun isUnsatWithAssumptions(p0: MutableCollection<BooleanFormula>): Boolean {
        error("Use isUnsat or check")
    }

    /**
     * boolean expressions which are not boolean literals
     */
    open val booleans: Set<BooleanFormula>
        get() = buildSet {
            formulas.forEach { f ->
                addAll(f
                    .getDeepestBooleanExprs(context.formulaManager)
                    .filter { !it.isCertainBool(fm.booleanFormulaManager) }
                )
            }
        }

    fun contains(constraint: BooleanFormula, switchable: Boolean, tag: String = ""): Boolean {
        return constraints.find {
            it.switchable == switchable &&
                    if (it is SwitchableConstraint)
                        it.original == constraint && it.tag == tag
                    else it.asFormula == constraint
        } != null
    }

    fun contains(constraint: BooleanFormula): Boolean {
        return constraints.find {
            if (it is SwitchableConstraint)
                it.original == constraint
            else it.asFormula == constraint
        } != null
    }

    open fun addConstraint(formula: BooleanFormula, switchable: Boolean, tag: String = ""): BooleanFormula {

        if (lastCheckAssumptions.isNotEmpty()) {
            pop()
            lastCheckAssumptions = emptyList()
        }

        if (contains(formula)) {
            logger().warn("You're trying to add ${if (switchable) "switchable" else "non-switchable"} constraint (tag=$tag) that already added before: $formula")

            if (switchable) {
                switchableConstraints.find { it.original == formula }?.also { enableConstraint(formula) } ?: run {
                    error("switchable constraint for $formula not found")
                }
            }

            needCheck()
            return formula
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
    final override fun addConstraint(f: BooleanFormula): Void? {
        addConstraint(f, false)
        return null
    }

    final override fun isUnsat(): Boolean = delegate.isUnsat

    fun check(reason: String = "common"): Status {
        if (!isCheckNeed && lastCheckAssumptions == assumptions)
            return lastCheckStatus!!

        if (lastCheckAssumptions.isNotEmpty()) {
            pop()
            lastCheckAssumptions = emptyList()

        }

        if (assumptions.isNotEmpty())
            push()

        lastCheckAssumptions = assumptions.toList()

        if (solverName != Solvers.Z3 && solverName != Solvers.BOOLECTOR) {
            assumptions.forEach { assumption ->
                delegate.addConstraintCustom(assumption)
            }
        }


        lastCheckStatus = try {
            val isUnsat = if (solverName == Solvers.Z3 || solverName == Solvers.BOOLECTOR)
                delegate.isUnsatWithAssumptions(assumptions)
            else delegate.isUnsat

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
        constraintsStack.push(currentLevelConstraints.toList())
        currentLevelConstraints.clear()
        delegate.push()
        needCheck()
    }

    override fun pop() {
        delegate.pop()
        currentLevelConstraints.addAll(constraintsStack.pop())
        needCheck()
    }


    override fun close() {
        currentLevelConstraints.clear()
        constraintsStack.clear()
        lastCheckAssumptions = emptyList()
        delegate.close()
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

    open fun readFromFile(file: File): List<Formula> {
        val formulas: List<Formula>

        when (solverName) {
            Solvers.Z3 -> {
                z3FromFile(file)
                formulas = z3Assertions()
                currentLevelConstraints.addAll(formulas.map { NonSwitchableConstraint(it as BooleanFormula) })
            }
            else -> TODO("read smtlib2 from file for non-z3 solver")
        }

        return formulas
    }

    fun Formula.isSuitable(): Boolean {
        return when (solverName) {
            Solvers.Z3 -> isZ3Formula()
            Solvers.CVC4 -> isCVC4Formula()
            Solvers.BOOLECTOR -> isBoolectorFormula()
            Solvers.PRINCESS -> isPrincessFormula()
            Solvers.SMTINTERPOL-> isSmtInterpolFormula()
            else -> error("Unsupported solver $solverName")
        }
    }
}