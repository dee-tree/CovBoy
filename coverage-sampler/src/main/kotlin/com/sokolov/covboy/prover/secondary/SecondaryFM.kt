package com.sokolov.covboy.prover.secondary

import com.sokolov.covboy.smt.isFormulaSupported
import org.sosy_lab.java_smt.SolverContextFactory
import org.sosy_lab.java_smt.api.Formula

abstract class SecondaryFM(
    override val mapper: FormulaMapper,
    override val secondarySolver: SolverContextFactory.Solvers
) : ISecondaryFM {

    override fun areSecondaryFormulas(vararg formulas: Formula) = formulas.all {
        secondarySolver.isFormulaSupported(it)
    }
}