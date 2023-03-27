package com.sokolov.covboy

import org.ksmt.KContext
import org.ksmt.expr.KExpr
import org.ksmt.solver.KSolver
import org.ksmt.solver.KSolverStatus
import org.ksmt.solver.z3.KZ3SMTLibParser
import org.ksmt.sort.KBoolSort
import org.ksmt.sort.KSort
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

fun KSolver<*>.ensureSat(msg: () -> String) {
    this.check().also { status ->
        if (status != KSolverStatus.SAT) {
            throw IllegalStateException("Formula is $status, but expected: ${KSolverStatus.SAT}. ${msg()}")
        }
    }
}

fun KContext.parseAssertions(input: File): List<KExpr<KBoolSort>> =
    KZ3SMTLibParser(this).parse(input.toPath())

fun KContext.parseAssertions(smtLibString: String): List<KExpr<KBoolSort>> =
    KZ3SMTLibParser(this).parse(smtLibString)

fun <S : KSort> KExpr<S>.isCovered(
    coverageSat: Set<KExpr<S>>,
    coverageUnsat: Set<KExpr<S>>,
    universe: Set<KExpr<S>>
): Boolean = (coverageSat + coverageUnsat).containsAll(universe)

fun <T : Any> T.logger(): Logger = LoggerFactory.getLogger(javaClass)

fun getOsName() = System.getProperty("os.name").lowercase()

val isWindows: Boolean
    get() = getOsName().startsWith("windows")

val isLinux: Boolean
    get() = getOsName().startsWith("linux")
