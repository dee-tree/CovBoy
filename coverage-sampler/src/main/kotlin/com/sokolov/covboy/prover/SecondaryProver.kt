package com.sokolov.covboy.prover

import com.sokolov.covboy.coverage.AtomCoverage
import com.sokolov.covboy.coverage.CoverageResult
import com.sokolov.covboy.coverage.EmptyAtomCoverage
import com.sokolov.covboy.smt.getBooleanValue
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.Formula
import org.sosy_lab.java_smt.api.ProverEnvironment
import org.sosy_lab.java_smt.api.SolverContext
import org.sosy_lab.java_smt.solvers.z3.z3FormulaTransform


open class SecondaryProver(
    private val delegate: ProverEnvironment,
    context: SolverContext,
    val z3Prover: BaseProverEnvironment,
) : Prover(delegate, context, z3Prover.formulas.map { it.z3FormulaTransform(z3Prover.context, context.formulaManager) }) {

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

    /**
     * mapper of master's (Z3) formula to this solver formula
     */
    private val mapper = mutableMapOf<Formula, Formula>()

    /**
     * Z3 formula -> this prover's formula
     */
    fun getFromMapper(f: Formula) : Formula? {
        return mapper[f]
    }

    fun getOriginalFormula(f: Formula) : Formula? {
        return mapper.entries.associate { it.value to it.key }[f]
    }


    fun getFromMapper(filter: (Formula) -> Boolean) : Formula {
        return mapper.entries.first { filter(it.key) }.value
    }

    fun putToMapper(original: Formula, thisFormula: Formula) {
        mapper[original] = thisFormula
    }

    init {
        z3Prover.formulas.map { mapper.getOrPut(it) { it.z3FormulaTransform(z3Prover.context, context.formulaManager) } }
    }

    override val booleans: Set<BooleanFormula>
        get() = z3Prover.booleans
            .map {
                mapper.getOrPut(it) {
                    it.z3FormulaTransform(z3Prover.context, context.formulaManager)
                } as BooleanFormula
            }
            .toSet()

    fun getOriginalCoverage(coverageResult: CoverageResult): CoverageResult = coverageResult.copy(
        atomsCoverage = coverageResult.atomsCoverage.map { atomCov ->
            val expr = mapper.entries.first { it.value == atomCov.expr }.key as BooleanFormula
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