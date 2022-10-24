package com.sokolov.covboy.prover.secondary

import com.sokolov.covboy.coverage.AtomCoverage
import com.sokolov.covboy.coverage.CoverageResult
import com.sokolov.covboy.coverage.EmptyAtomCoverage
import com.sokolov.covboy.prover.BaseProverEnvironment
import com.sokolov.covboy.prover.Prover
import com.sokolov.covboy.smt.getBooleanValue
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.FormulaManager
import org.sosy_lab.java_smt.api.ProverEnvironment
import org.sosy_lab.java_smt.api.SolverContext


open class SecondaryProver(
    delegate: ProverEnvironment,
    context: SolverContext,
    val z3Prover: BaseProverEnvironment,
    private val mapper: FormulaMapper = FormulaMapper(z3Prover.context, z3Prover.fm, context, context.formulaManager),
    delegateFm: FormulaManager = SecondaryFormulaManager(z3Prover.fm, context.formulaManager, context.solverName, mapper),
    ) : Prover(delegate, context, emptyList()) {

    override val fm: FormulaManager = delegateFm

    constructor(
        context: SolverContext,
        z3Prover: BaseProverEnvironment,
    ) : this(
        context.newProverEnvironment(
            SolverContext.ProverOptions.GENERATE_MODELS,
            SolverContext.ProverOptions.ENABLE_SEPARATION_LOGIC,
            SolverContext.ProverOptions.GENERATE_UNSAT_CORE,
        ), context, z3Prover
    )

    init {
        z3Prover.formulas.forEach {
            addConstraint(mapper.toSecondary(it))
        }
    }

    override val booleans: Set<BooleanFormula>
        get() = z3Prover.booleans
            .map { mapper.toSecondary(it) }
            .toSet()

    fun getOriginalCoverage(coverageResult: CoverageResult): CoverageResult = coverageResult.copy(
        atomsCoverage = coverageResult.atomsCoverage.map { atomCov ->
            val expr = mapper.findOriginal(atomCov.expr) ?: error("Not found atom in mapper")
            when (atomCov) {
                is EmptyAtomCoverage -> atomCov.copy(expr)
                is AtomCoverage -> atomCov.copy(expr, atomCov.values.map {
                    z3Prover.context.formulaManager.booleanFormulaManager.makeBoolean(
                        it.getBooleanValue(context.formulaManager.booleanFormulaManager)
                    )
                }.toSet())
            }
        }.toSet()
    )

    override fun toString(): String {
        return "SecondaryProver($solverName from ${z3Prover.solverName})"
    }
}