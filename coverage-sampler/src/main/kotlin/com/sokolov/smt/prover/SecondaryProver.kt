package com.sokolov.smt.prover

import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.Formula
import org.sosy_lab.java_smt.api.ProverEnvironment
import org.sosy_lab.java_smt.api.SolverContext
import org.sosy_lab.java_smt.solvers.z3.z3FormulaTransform


class SecondaryProver(
    private val delegate: ProverEnvironment,
    context: SolverContext,
    z3Formulas: Collection<BooleanFormula>,

    private val z3Prover: IProver,
) : Prover(delegate, context, z3Formulas.map { it.z3FormulaTransform(z3Prover.context, context.formulaManager) }) {

    /**
     * mapper of master's formula to this solver formula
     */
    private val mapper = mutableMapOf<Formula, Formula>()

    init {
        z3Formulas.map { mapper.getOrPut(it) { it.z3FormulaTransform(z3Prover.context, context.formulaManager) } }
    }

    override val booleans: Set<BooleanFormula>
        get() = z3Prover.booleans
            .map {
                mapper.getOrPut(it) {
                    it.z3FormulaTransform(z3Prover.context, context.formulaManager)
                } as BooleanFormula
            }
            .toSet()

}