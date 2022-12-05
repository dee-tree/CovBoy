package com.sokolov.covboy.solvers.provers

import com.sokolov.covboy.solvers.formulas.Constraint
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.Formula
import org.sosy_lab.java_smt.api.Model
import java.io.File

interface ExtProverEnvironment {
    fun addConstraint(constraint: Constraint)
    fun addConstraintsFromFile(smtFile: File): List<Constraint>

    fun isSuitableFormula(formula: Formula): Boolean

    fun pop()
    fun push()

    val pushScopesSize: Int
    fun checkSat(assumptions: List<BooleanFormula> = emptyList()): Status

    fun getModel(): Model
    val modelAssignments: List<Model.ValueAssignment>

    fun getUnsatCore(): List<BooleanFormula>

    fun reset()
    fun close()

}