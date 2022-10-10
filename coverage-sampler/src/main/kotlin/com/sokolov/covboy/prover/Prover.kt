package com.sokolov.covboy.prover

import org.sosy_lab.java_smt.SolverContextFactory
import org.sosy_lab.java_smt.api.*
import org.sosy_lab.java_smt.solvers.boolector.BoolectorUnsatCoreWithAssumptions
import org.sosy_lab.java_smt.solvers.z3.addConstraintCustom
import java.io.File


open class Prover(
    private val delegate: ProverEnvironment,
    override val context: SolverContext,
    formulas: Collection<BooleanFormula>
) : IProver, ProverEnvironment by delegate {

    protected val _constraints = mutableListOf<BooleanFormula>()

    override val constraints: List<BooleanFormula>
        get() = _constraints.toList()

    // checks optimization via last status remember
    private var lastCheckStatus: Status? = null
    protected var isCheckNeed: Boolean = true
        private set

    private val checksMap = mutableMapOf<String, MutableChecksCounter>()

    override val checksStatistics: Map<String, ChecksCounter>
        get() = checksMap


    init {
        formulas.forEach(::addConstraint)
    }

    constructor(
        delegate: ProverEnvironment,
        context: SolverContext,
        formulaInputFile: File
    ) : this(delegate, context, context.formulaManager.readFormulasFromSmtLib(formulaInputFile))


    override val assertionsStorage: AssertionsStorage = AssertionsStorage(this, ::onAssertionChanged)

    override val unsatCoreWithAssumptions: List<BooleanFormula>
        get() {
            if (solver != SolverContextFactory.Solvers.BOOLECTOR)
            return unsatCore

            // bolector unsat core

            return this.BoolectorUnsatCoreWithAssumptions()
        }

    /**
     * Add switchable constraint (with put in assertions storage)
     */
    override fun addConstraint(constraint: BooleanFormula, tag: String): Assertion {
        return assertionsStorage.assert(constraint, tag)
    }

    /**
     * add hard constraint without put in assertions storage
     */
    override fun addConstraint(constraint: BooleanFormula): Void? {
        if (!isCheckNeed) {
            pop()
        }

        return delegate
            .addConstraintCustom(constraint)
            .also {
                isCheckNeed = true
                _constraints.add(constraint)
            }
    }

    override fun filterAssertions(filter: (Assertion) -> Boolean): List<Assertion> {
        return assertionsStorage.assertions.filter(filter)
    }


    override val booleans: Set<BooleanFormula>
        get() = buildSet {
            constraints.forEach { constraint ->
                addAll(constraint.getDeepestBooleanExprs(context.formulaManager))
            }
        }

    override fun check(reason: String): Status = check(emptyList(), reason)

    override fun check(assumptions: List<BooleanFormula>, reason: String): Status {

        if (!isCheckNeed)
            return lastCheckStatus!!

        lastCheckStatus = try {
            push()
            (assumptions + assertionsStorage.assumptions).toSet().forEach { assumption ->
                addConstraint(assumption)
            }

            val isUnsat = this.isUnsat
            if (isUnsat) Status.UNSAT else Status.SAT
        } catch (e: SolverException) {
            Status.UNKNOWN
        }
        isCheckNeed = false

        checksMap.getOrPut(reason) { MutableChecksCounter() }.update(lastCheckStatus!!)

        if (lastCheckStatus == Status.UNKNOWN)
            throw IllegalStateException("got unknown status on solver check...")

        return lastCheckStatus!!
    }

    override fun push() {
        delegate.push()
        isCheckNeed = true
    }

    override fun pop() {
        delegate.pop()
        isCheckNeed = true
    }

    final override fun addConstraintsFromSmtLib(input: File): List<BooleanFormula> {
        println("File: ${input.absolutePath}")
        return context.formulaManager.readFormulasFromSmtLib(input).onEach(::addConstraint).also { isCheckNeed = true }
    }

    private fun onAssertionChanged(newState: AssertionState) {
        isCheckNeed = true
    }

    override fun getAssertionsByTag(onTag: (String) -> Boolean): List<Assertion> = assertionsStorage.getByTag(onTag)
    override fun getAssertionsByTag(tag: String): List<Assertion> = assertionsStorage.getByTag(tag)

    override fun toString(): String {
        return "Prover($solver)"
    }
}

// line-based content
// https://github.com/sosy-lab/java-smt/blob/master/src/org/sosy_lab/java_smt/example/FormulaClassifier.java
fun FormulaManager.readFormulasFromSmtLib(input: File): List<BooleanFormula> {
    val definitions = mutableListOf<String>()
    val formulas = mutableListOf<BooleanFormula>()

    input.forEachLine { line ->
        if (listOf(";", "(push ", "(pop ", "(reset", "(set-logic").any { line.startsWith(it) }) {
        } else if (line.startsWith("(assert ")) {
            val formula = parse(definitions.joinToString(" ") + " " + line)
            formulas.add(formula)
        } else {
            // definition
            definitions.add(line)
        }
    }

    return formulas
}