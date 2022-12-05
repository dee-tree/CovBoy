package com.sokolov.covboy.solvers.theories

import com.sokolov.covboy.solvers.theories.Theories.*
import org.sosy_lab.java_smt.SolverContextFactory.Solvers

val Solvers.supportedTheories: Set<Theories>
    get() = when (this) {
        Solvers.Z3 -> Z3Theories
        Solvers.BOOLECTOR -> BoolectorTheories
        Solvers.CVC4 -> CVC4Theories
        Solvers.MATHSAT5 -> MathSat5Theories
        Solvers.PRINCESS -> PrincessTheories
        Solvers.SMTINTERPOL -> SmtInterpolTheories
        Solvers.YICES2 -> Yices2Theories
        else -> emptySet()
    }

private val Z3Theories = setOf(
    INTEGER, RATIONAL, ARRAY, BITVECTOR, FLOAT, UF, QUANTIFIER
)

private val BoolectorTheories = setOf(
    ARRAY, BITVECTOR, UF, QUANTIFIER
)

private val CVC4Theories = setOf(
    INTEGER, RATIONAL, ARRAY, BITVECTOR, FLOAT, UF, QUANTIFIER
)

private val MathSat5Theories = setOf(
    INTEGER, RATIONAL, ARRAY, BITVECTOR, FLOAT, UF
)

private val PrincessTheories = setOf(
    INTEGER, ARRAY, BITVECTOR, UF, QUANTIFIER
)

private val SmtInterpolTheories = setOf(
    INTEGER, RATIONAL, ARRAY, UF
)

private val Yices2Theories = setOf(
    INTEGER, RATIONAL, BITVECTOR, UF, QUANTIFIER
)