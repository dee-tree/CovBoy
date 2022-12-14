package com.sokolov.covboy.utils

import com.sokolov.covboy.coverage.AtomCoverage
import com.sokolov.covboy.coverage.CoverageResult
import com.sokolov.covboy.coverage.EmptyAtomCoverage
import com.sokolov.covboy.solvers.formulas.utils.getBooleanLiteralValue
import com.sokolov.covboy.solvers.provers.secondary.SecondaryProver
import org.sosy_lab.java_smt.api.*
import org.sosy_lab.java_smt.api.Model.ValueAssignment
import org.sosy_lab.java_smt.basicimpl.AbstractFormulaManager
import org.sosy_lab.java_smt.basicimpl.FunctionDeclarationImpl

fun SecondaryProver.getPrimaryCoverage(coverageResult: CoverageResult): CoverageResult = coverageResult.copy(
    atomsCoverage = coverageResult.atomsCoverage.map { atomCov ->
        val expr = this.findPrimary(atomCov.expr) ?: error("atom in mapper of Secondary Prover NOT FOUND")
        when (atomCov) {
            is EmptyAtomCoverage -> atomCov.copy(expr)
            is AtomCoverage -> atomCov.copy(expr, atomCov.values.map {
                baseProver.fm.booleanFormulaManager.makeBoolean(
                    fm.booleanFormulaManager.getBooleanLiteralValue(it)
                )
            }.toSet())
        }
    }.toSet()
)

fun Pair<BooleanFormula, BooleanFormula>.makeAssignment(fm: BooleanFormulaManager): ValueAssignment = ValueAssignment(
    first,
    second,
    fm.equivalence(first, second),
    "eq of $first - $second",
    fm.getBooleanLiteralValue(second),
    emptyList<Any>()
)