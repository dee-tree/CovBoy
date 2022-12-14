package com.sokolov.covboy.solvers.provers.secondary

import com.sokolov.covboy.solvers.formulas.Constraint
import com.sokolov.covboy.solvers.formulas.utils.asFormula
import com.sokolov.covboy.solvers.formulas.utils.doesSupportFormula
import com.sokolov.covboy.solvers.provers.Prover
import com.sokolov.covboy.solvers.provers.Status
import com.sokolov.covboy.solvers.provers.secondary.fm.SecondaryFormulaManager
import com.sokolov.covboy.solvers.provers.wrap.wrap
import org.sosy_lab.java_smt.api.*
import org.sosy_lab.java_smt.solvers.z3.z3FormulaTransform
import java.io.File


open class SecondaryProver internal constructor(
    delegate: ConstraintStoredProver,
    context: SolverContext,
    val baseProver: Prover,
    private val mapper: FormulaMapper = FormulaMapper(
        baseProver.context,
        baseProver.fm,
        context,
        context.formulaManager
    ),
    delegateFm: FormulaManager = SecondaryFormulaManager(
        baseProver.fm,
        context.formulaManager,
        context.solverName,
        mapper
    ),
) : Prover(delegate, context) {

    override val fm: FormulaManager = delegateFm

    constructor(
        delegate: ProverEnvironment,
        context: SolverContext,
        baseProver: Prover,
    ) : this(delegate.wrap(context.solverName), context, baseProver)

    init {
        addOriginalConstraints()
    }

    fun addOriginalConstraints() {
        baseProver.constraints.forEach {
            addOnlySecondaryConstraint(mapper.toSecondary(it))
        }
    }

    fun addOnlySecondaryConstraint(constraint: Constraint) {
        println("Add only secondary constraint: $constraint")
        super.addConstraint(constraint)
    }

    override fun addConstraint(constraint: Constraint) {
        println("add secondary constraint: $constraint")
        check(solverName.doesSupportFormula(constraint.asFormula) || baseProver.solverName.doesSupportFormula(constraint.asFormula))

        if (solverName.doesSupportFormula(constraint.asFormula)) {
            baseProver.addConstraint(mapper.findOriginal(constraint)!!)
            addOnlySecondaryConstraint(constraint)
        } else {
            baseProver.addConstraint(constraint)
            addOnlySecondaryConstraint(mapper.toSecondary(constraint))
        }
    }

    override fun addConstraintsFromFile(smtFile: File): List<Constraint> {
        val primaryConstraints = baseProver.addConstraintsFromFile(smtFile)

        val secondaryConstraints = primaryConstraints.map { mapper.toSecondary(it) }
        secondaryConstraints.forEach(this::addOnlySecondaryConstraint)
        return secondaryConstraints
    }

    override fun checkSat(assumptions: List<BooleanFormula>): Status {
        val secondaryStatus = super.checkSat(assumptions)
        val primaryStatus = baseProver.checkSat(assumptions)
        check(secondaryStatus == primaryStatus)
        return secondaryStatus
    }

    override val modelAssignments: List<Model.ValueAssignment>
        get() = super.modelAssignments.also { secondaryModel ->
            baseProver.modelAssignments.forEach {
                mapper.toSecondary(it.key)
                mapper.toSecondary(it.valueAsFormula)
                mapper.toSecondary(it.asFormula(baseProver.fm))
//                mapper.toSecondary(it.assignmentAsFormula.z3FormulaTransform(baseProver.context, baseProver.fm))
            }
        }

    override val booleans: Set<BooleanFormula>
        get() = baseProver.booleans
            .map { mapper.toSecondary(it) }
            .toSet()

    fun <T : Formula> findPrimary(f: T): T? = mapper.findOriginal(f)

    override fun pop() {
        super.pop()
        baseProver.pop()
    }

    override fun push() {
        super.push()
        baseProver.push()
    }

    override fun reset() {
        mapper.clear()
        super.reset()
    }

    override fun close() {
        mapper.clear()
        super.close()
        baseProver.close()
        baseProver.context.close()
    }

    override fun toString(): String {
        return "SecondaryProver($solverName from ${baseProver.solverName})"
    }
}