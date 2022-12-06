package com.sokolov.covboy.solvers.provers.secondary

import com.sokolov.covboy.solvers.formulas.Constraint
import com.sokolov.covboy.solvers.formulas.SwitchableConstraint
import com.sokolov.covboy.solvers.provers.ExtProverEnvironment
import com.sokolov.covboy.solvers.provers.Status
import org.sosy_lab.java_smt.api.BooleanFormula
import java.io.File
import java.util.*

interface ConstraintStoredProver : ExtProverEnvironment {
    val constraints: List<Constraint>
    val switchableConstraints: List<SwitchableConstraint>
    val assumptions: List<BooleanFormula>
}

abstract class AbstractConstraintStoredProver constructor(
    protected val delegate: ExtProverEnvironment
) : ConstraintStoredProver, ExtProverEnvironment by delegate {
    protected val constraintsStack: Stack<List<Constraint>> = Stack()
    protected val currentLevelConstraints = mutableListOf<Constraint>()

    override val switchableConstraints: List<SwitchableConstraint>
        get() = constraints.filterIsInstance<SwitchableConstraint>()

    override val constraints: List<Constraint>
        get() = constraintsStack.fold(emptyList<Constraint>()) { curr, acc -> acc + curr } + currentLevelConstraints

    override val assumptions: List<BooleanFormula>
        get() = switchableConstraints.filter { it.enabled }.map { it.assumption }

    override fun checkSat(assumptions: List<BooleanFormula>): Status {
        return delegate.checkSat(assumptions + this.assumptions)
    }

    override fun addConstraint(constraint: Constraint) {
        delegate.addConstraint(constraint)
        currentLevelConstraints.add(constraint)
    }

    override fun addConstraintsFromFile(smtFile: File): List<Constraint> = delegate.addConstraintsFromFile(smtFile)
        .also { currentLevelConstraints.addAll(it) }

    override fun reset() {
        currentLevelConstraints.clear()
        constraintsStack.clear()
        delegate.reset()
    }

    override fun push() {
        constraintsStack.push(currentLevelConstraints.toList())
        currentLevelConstraints.clear()
        delegate.push()
    }

    override fun pop() {
        currentLevelConstraints.clear()
        currentLevelConstraints.addAll(constraintsStack.pop())
        delegate.pop()
    }

    override fun close() {
        currentLevelConstraints.clear()
        constraintsStack.clear()
        delegate.close()
    }
}