package com.sokolov.covboy.solvers.provers.secondary.fm

import com.sokolov.covboy.solvers.formulas.utils.doesSupportFormula
import com.sokolov.covboy.solvers.provers.secondary.FormulaMapper
import org.sosy_lab.java_smt.SolverContextFactory
import org.sosy_lab.java_smt.api.Formula
import org.sosy_lab.java_smt.api.FormulaManager

abstract class SecondaryFM(
    override val mapper: FormulaMapper,
    override val secondarySolver: SolverContextFactory.Solvers,
    private val originalFm: FormulaManager,
    private val secondaryFm: FormulaManager
) : ISecondaryFM {

    override fun areSecondaryFormulas(vararg formulas: Formula) = formulas.all {
        secondarySolver.doesSupportFormula(it)
    }

    override fun areAnySecondaryFormula(vararg formulas: Formula): Boolean = formulas.any {
        secondarySolver.doesSupportFormula(it)
    }

    override fun <T : Formula> T.asOriginal(): T = this.asOriginalOrNull() ?: error("not found original term $this")

}