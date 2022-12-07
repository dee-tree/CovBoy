package com.sokolov.covboy.solvers.provers.secondary

import com.sokolov.covboy.solvers.formulas.Constraint
import com.sokolov.covboy.solvers.formulas.utils.doesSupportFormula
import com.sokolov.covboy.solvers.provers.ExtProverEnvironment
import com.sokolov.covboy.solvers.provers.Prover
import com.sokolov.covboy.solvers.provers.secondary.fm.SecondaryFormulaManager
import com.sokolov.covboy.solvers.provers.wrap.wrap
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.Formula
import org.sosy_lab.java_smt.api.FormulaManager
import org.sosy_lab.java_smt.api.ProverEnvironment
import org.sosy_lab.java_smt.api.SolverContext
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
    ) : this(
        delegate.wrap(context.solverName),
        /*context.newProverEnvironment(
            SolverContext.ProverOptions.GENERATE_MODELS,
            SolverContext.ProverOptions.ENABLE_SEPARATION_LOGIC,
            SolverContext.ProverOptions.GENERATE_UNSAT_CORE,
        )*/ context, baseProver
    )

    init {
        addOriginalConstraints()
    }

    fun addOriginalConstraints() {
        baseProver.constraints.forEach {
            addConstraint(mapper.toSecondary(it))
        }
    }

    override fun addConstraint(constraint: Constraint) {
        check(solverName.doesSupportFormula(constraint.asFormula) || baseProver.solverName.doesSupportFormula(constraint.asFormula))
        super.addConstraint(
            if (solverName.doesSupportFormula(constraint.asFormula)) constraint else mapper.toSecondary(
                constraint
            )
        )
    }

    /*override fun addConstraint(formula: BooleanFormula, switchable: Boolean, tag: String): BooleanFormula {
        return super.addConstraint(if (formula.isSuitable()) formula else mapper.toSecondary(formula), switchable, tag)
    }*/

    override fun addConstraintsFromFile(smtFile: File): List<Constraint> {
        val primaryConstraints = baseProver.addConstraintsFromFile(smtFile)

        val secondaryConstraints = primaryConstraints.map { mapper.toSecondary(it) }
        secondaryConstraints.forEach(this::addConstraint)
        return secondaryConstraints
    }

    override val booleans: Set<BooleanFormula>
        get() = baseProver.booleans
            .map { mapper.toSecondary(it) }
            .toSet()

    fun <T : Formula> findPrimary(f: T): T? = mapper.findOriginal(f)

    // TODO: move out get original coverage
    /*fun getOriginalCoverage(coverageResult: CoverageResult): CoverageResult = coverageResult.copy(
        atomsCoverage = coverageResult.atomsCoverage.map { atomCov ->
            val expr = mapper.findOriginal(atomCov.expr) ?: error("Not found atom in mapper")
            when (atomCov) {
                is EmptyAtomCoverage -> atomCov.copy(expr)
                is AtomCoverage -> atomCov.copy(expr, atomCov.values.map {
                    baseProver.context.formulaManager.booleanFormulaManager.makeBoolean(
                        it.getBooleanValue(context.formulaManager.booleanFormulaManager)
                    )
                }.toSet())
            }
        }.toSet()
    )*/

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