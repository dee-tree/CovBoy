package com.sokolov.covboy.prover.secondary

import org.sosy_lab.java_smt.SolverContextFactory
import org.sosy_lab.java_smt.api.Formula

interface ISecondaryFM {

    val mapper: FormulaMapper
    val secondarySolver: SolverContextFactory.Solvers

    fun areSecondaryFormulas(vararg formulas: Formula): Boolean
    fun areAnySecondaryFormula(vararg formulas: Formula): Boolean
    fun <T : Formula> T.asOriginal(): T
    fun <T : Formula> T.asOriginalOrNull(): T?
}