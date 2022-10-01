package com.sokolov.smt.prover

import com.sokolov.smt.Status
import org.sosy_lab.java_smt.api.*
import java.io.File


open class Prover(
    private val delegate: ProverEnvironment,
    override val context: SolverContext,
    formulas: Collection<BooleanFormula>
) : IProver, ProverEnvironment by delegate {

    protected val _constraints = mutableListOf<BooleanFormula>()

    override val constraints: List<BooleanFormula>
        get() = _constraints.toList()


    init {
        formulas.forEach(::addConstraint)
    }

    constructor(
        delegate: ProverEnvironment,
        context: SolverContext,
        formulaInputFile: File
    ) : this(delegate, context, context.formulaManager.readFormulasFromSmtLib(formulaInputFile)) {
        addConstraintsFromSmtLib(formulaInputFile)
    }


    override fun addConstraint(constraint: BooleanFormula): Void? = delegate
        .addConstraint(constraint)
        .also { _constraints.add(constraint) }


    override val booleans: Set<BooleanFormula>
        get() = buildSet {
            constraints.forEach { constraint ->
                addAll(constraint.getDeepestBooleanExprs(context.formulaManager))
            }
        }

    override fun check(): Status = check(emptyList())

    override fun check(assumptions: List<BooleanFormula>): Status {
        return try {
            val isUnsat = this.isUnsatWithAssumptions(assumptions)
            if (isUnsat) Status.UNSAT else Status.SAT
        } catch (e: SolverException) {
            Status.UNKNOWN
        }
    }

    final override fun addConstraintsFromSmtLib(input: File): List<BooleanFormula> {
        println("File: ${input.absolutePath}")
        return context.formulaManager.readFormulasFromSmtLib(input).onEach(::addConstraint)
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
            val formula = parse(definitions.joinToString("\n") + "\n" + line)
            formulas.add(formula)
        } else {
            // definition
            definitions.add(line)
        }
    }

    return formulas
}