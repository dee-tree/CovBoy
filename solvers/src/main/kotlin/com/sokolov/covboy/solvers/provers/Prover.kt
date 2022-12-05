package com.sokolov.covboy.solvers.provers

import com.sokolov.covboy.solvers.formulas.Constraint
import com.sokolov.covboy.solvers.formulas.MutableSwitchableConstraint
import com.sokolov.covboy.solvers.formulas.NonSwitchableConstraint
import com.sokolov.covboy.solvers.formulas.SwitchableConstraint
import com.sokolov.covboy.solvers.formulas.utils.isBooleanLiteral
import com.sokolov.covboy.solvers.provers.secondary.ConstraintStoredProver
import com.sokolov.covboy.solvers.provers.wrap.wrap
import com.sokolov.covboy.solvers.util.logger
import org.sosy_lab.java_smt.SolverContextFactory.Solvers
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.FormulaManager
import org.sosy_lab.java_smt.api.ProverEnvironment
import org.sosy_lab.java_smt.api.SolverContext
import org.sosy_lab.java_smt.api.getDeepestBooleanExprs
import java.util.*

open class Prover internal constructor(
    open val delegate: ConstraintStoredProver,
    val context: SolverContext,
) : ExtProverEnvironment by delegate {

    constructor(delegate: ProverEnvironment, context: SolverContext): this(delegate.wrap(context.solverName), context)

    val solverName: Solvers
        get() = context.solverName

    open val fm: FormulaManager
        get() = context.formulaManager

    private val _checkCounter = MutableChecksCounter()
    val checkCounter: ChecksCounter
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

//    init {
//        push()
//    }

    override fun reset() {
        while (pushScopesSize > 0) {
            pop()
        }

        currentLevelConstraints.clear()
        constraintsStack.clear()
        lastCheckAssumptions = emptyList()

        delegate.reset()
    }

    protected fun needCheck() {
        isCheckNeed = true
    }

    override fun getUnsatCore(): List<BooleanFormula> {
        val unsatCore = delegate.getUnsatCore()

        val enabledSwConstraints = switchableConstraints.filter { it.enabled }

        /*
            fix to get only assumptions from unsat core, because sometimes it can return full expression,
            which was assumed (assumption -> expr. We need - assumption, but can get "assumption -> expr").
            Reproduced in: Princess
            */
        return unsatCore
        return unsatCore.map { ucExpr ->
            enabledSwConstraints.find { /*java smt unsat core (princess, smtinterpol, cvc4)*/it.asFormula == ucExpr || it.assumption == ucExpr /*Z3 returns assumption*/ }?.original
                ?: ucExpr
        }.toSet().toList()
    }

    fun getUnsatCoreConstraints(): List<Constraint> {
        val unsatCore = getUnsatCore()
        val enabledSwConstraints = switchableConstraints.filter { it.enabled }
        return unsatCore.map { unsatCoreExpr ->
            enabledSwConstraints.first { it.assumption == unsatCoreExpr || it.asFormula == unsatCoreExpr }
        }
    }

    /**
     * boolean expressions which are not boolean literals
     */
    open val booleans: Set<BooleanFormula>
        get() = buildSet {
            formulas.forEach { f ->
                addAll(f
                    .getDeepestBooleanExprs(context.formulaManager)
                    .filter { !it.isBooleanLiteral(fm.booleanFormulaManager) }
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


    override fun addConstraint(constraint: Constraint) {
//        if (lastCheckAssumptions.isNotEmpty()) {
//            pop()
//            lastCheckAssumptions = emptyList()
//        }


        if (constraint in constraints) {
            logger().warn("You're trying to add ${if (constraint.switchable) "switchable" else "non-switchable"} constraint that already added before: $constraint")

            if (constraint.switchable) {
                switchableConstraints.find { it == constraint }?.also { enableConstraint(constraint) } ?: run {
                    error("switchable constraint for $constraint not found")
                }
            }

            needCheck()
            return
        }

        delegate.addConstraint(constraint)
        currentLevelConstraints.add(constraint)

        needCheck()
    }


    override fun checkSat(assumptions: List<BooleanFormula>): Status {
        val assumptions = (this.assumptions + assumptions).toSet().toList()
        if (!isCheckNeed && lastCheckAssumptions == assumptions)
            return lastCheckStatus!!


//        if (lastCheckAssumptions.isNotEmpty()) {
//            pop()
//            lastCheckAssumptions = emptyList()
//        }

//        if (assumptions.isNotEmpty())
//            push()

        lastCheckAssumptions = assumptions.toList()

        lastCheckStatus = delegate.checkSat(assumptions)

        isCheckNeed = false
        _checkCounter.update(lastCheckStatus!!)
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

    fun disableConstraint(constraint: Constraint) {
        switchableConstraints.find { it == constraint }?.disable()
            ?: error("It's not possible to disable the assertion which is not added already")

        needCheck()
    }

    fun disableConstraint(formula: BooleanFormula) {
        switchableConstraints.find { it.original == formula }?.disable()
            ?: error("It's not possible to disable the assertion which is not added already")

        needCheck()
    }

    fun enableConstraint(constraint: Constraint) {
        switchableConstraints.find { it == constraint }?.enable()
            ?: error("It's not possible to enable the assertion which is not added already")

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

    override fun toString(): String {
        return "Prover($solverName)"
    }
}