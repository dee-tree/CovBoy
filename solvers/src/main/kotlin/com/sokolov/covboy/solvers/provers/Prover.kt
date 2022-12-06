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

open class Prover internal constructor(
    open val delegate: ConstraintStoredProver,
    val context: SolverContext,
) : ConstraintStoredProver by delegate {

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

    // checks optimization via last status remember
    private var lastCheckStatus: Status? = null
    private var isCheckNeed: Boolean = true


    override fun reset() {
        while (pushScopesSize > 0) {
            pop()
        }

        delegate.reset()
    }

    protected fun needCheck() {
        isCheckNeed = true
    }

    override fun getUnsatCore(): List<BooleanFormula> {
        return delegate.getUnsatCore()
    }

    fun getUnsatCoreConstraints(): List<Constraint> {
        val unsatCore = getUnsatCore()
        val enabledSwConstraints = switchableConstraints.filter { it.enabled }
        return unsatCore.map { unsatCoreExpr ->
            enabledSwConstraints.first { it.original == unsatCoreExpr }
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

        needCheck()
    }


    override fun checkSat(assumptions: List<BooleanFormula>): Status {
        if (!isCheckNeed)
            return lastCheckStatus!!

        lastCheckStatus = delegate.checkSat(assumptions)

        isCheckNeed = false
        _checkCounter.update(lastCheckStatus!!)
        return lastCheckStatus!!
    }


    override fun push() {
        delegate.push()
        needCheck()
    }

    override fun pop() {
        delegate.pop()
        needCheck()
    }


    override fun close() {
        delegate.close()
    }

    fun disableConstraint(constraint: Constraint) {
        (switchableConstraints.find { it == constraint } as? MutableSwitchableConstraint)?.disable()
            ?: error("It's not possible to disable the assertion which is not added already")

        needCheck()
    }

    fun disableConstraint(formula: BooleanFormula) {
        (switchableConstraints.find { it.original == formula } as? MutableSwitchableConstraint)?.disable()
            ?: error("It's not possible to disable the assertion which is not added already")

        needCheck()
    }

    fun enableConstraint(constraint: Constraint) {
        (switchableConstraints.find { it == constraint } as? MutableSwitchableConstraint)?.enable()
            ?: error("It's not possible to enable the assertion which is not added already")

        needCheck()
    }

    fun enableConstraint(formula: BooleanFormula) {
        (switchableConstraints.find { it.original == formula } as? MutableSwitchableConstraint)?.enable()
            ?: error("It's not possible to enable the assertion which is not added already")

        needCheck()
    }

//    val enabledSwitchableConstraints: List<BooleanFormula>
//        get() = switchableConstraints.filter { it.enabled }.map { it.original }

//    fun filterSwitchableConstraints(filter: (SwitchableConstraint) -> Boolean): List<BooleanFormula> {
//        return switchableConstraints.filter(filter).map { it.original }
//    }

    override fun toString(): String {
        return "Prover($solverName)"
    }
}