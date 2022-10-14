package com.sokolov.covboy.prover

import com.sokolov.covboy.prover.assertions.AssertionListener
import org.sosy_lab.java_smt.SolverContextFactory.Solvers
import org.sosy_lab.java_smt.api.*
import org.sosy_lab.java_smt.solvers.boolector.boolectorUnsatCoreWithAssumptions
import org.sosy_lab.java_smt.solvers.boolector.isBoolectorFormula
import org.sosy_lab.java_smt.solvers.cvc4.isCVC4Formula
import org.sosy_lab.java_smt.solvers.z3.addConstraintCustom
import org.sosy_lab.java_smt.solvers.z3.isZ3Formula
import java.util.*
import java.util.concurrent.TimeoutException

abstract class BaseProverEnvironment(
    private val delegate: ProverEnvironment,
    val context: SolverContext,
    protected val assertionStorage: AssertionsStorage,
    val timeoutOnCheck: Long = 60_000L
) : ProverEnvironment by delegate {

    init {
        assertionStorage.assertionListener = object : AssertionListener {
            override fun onAssertionEnabled(assertion: Assertion) = needCheck()
            override fun onAssertionDisabled(assertion: Assertion) = needCheck()
        }
    }

    val solverName: Solvers
        get() = context.solverName

    val fm: FormulaManager
        get() = context.formulaManager

    private val _checkCounter = mutableMapOf<String, MutableChecksCounter>()
    val checkCounter: Map<String, ChecksCounter>
        get() = _checkCounter

    val assertions: Set<Assertion>
        get() = assertionStorage.assertions

    val assumptions: List<BooleanFormula>
        get() = assertionStorage.assumptions

    private val constraintsStack: Stack<List<BooleanFormula>> = Stack()
    private val currentLevelConstraints = mutableListOf<BooleanFormula>()

    val constraints: List<BooleanFormula>
        get() = constraintsStack.fold(emptyList<BooleanFormula>()) { curr, acc -> acc + curr } + currentLevelConstraints


    // checks optimization via last status remember
    private var lastCheckStatus: Status? = null
    private var isCheckNeed: Boolean = true

    protected fun needCheck() {
        isCheckNeed = true
    }

    val unsatCoreWithAssumptions: List<BooleanFormula>
        get() {
            if (solverName != Solvers.BOOLECTOR)
                return unsatCore

            // bolector unsat core
            return this.boolectorUnsatCoreWithAssumptions()
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
            constraints.forEach { constraint -> addAll(constraint.getDeepestBooleanExprs(context.formulaManager)) }
        }

    /**
     * Add switchable constraint (with put in assertions storage)
     */
    fun addConstraint(constraint: BooleanFormula, tag: String): Assertion {
        return assertionStorage.assert(constraint, tag)
    }

    /**
     * add hard constraint without put in assertions storage
     */
    override fun addConstraint(constraint: BooleanFormula): Void? {
        if (!isCheckNeed) {
            pop()
        }

        delegate.addConstraintCustom(constraint)
        needCheck()
        currentLevelConstraints.add(constraint)
        return null
    }

    final override fun isUnsat(): Boolean = withTimeout(
        timeoutOnCheck,
        { delegate.isUnsat },
        { /* on timeout */
            /* shutdownManager.requestShutdown("timeout $checkTimeOut ms exceeded") */
            throw TimeoutException("Timeout $timeoutOnCheck ms exceeded on sat-check")
        }
    )

    fun check(reason: String = "common"): Status = check(emptyList(), reason)

    fun check(assumptions: List<BooleanFormula>, reason: String = "common"): Status {
        if (!isCheckNeed)
            return lastCheckStatus!!

        push()

        (assumptions + this.assumptions).toSet().forEach { assumption ->
            addConstraintCustom(assumption)
        }

        lastCheckStatus = try {
            val isUnsat = this.isUnsat
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
        isCheckNeed = true
    }

    override fun pop() {
        delegate.pop()
        currentLevelConstraints.clear()
        currentLevelConstraints.addAll(constraintsStack.pop().toMutableList())
        isCheckNeed = true
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