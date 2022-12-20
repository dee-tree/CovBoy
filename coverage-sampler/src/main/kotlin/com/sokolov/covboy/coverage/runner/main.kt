package com.sokolov.covboy.coverage.runner

import com.sokolov.covboy.coverage.FormulaCoverage
import com.sokolov.covboy.coverage.predicate.bool.BoolPredicatesExtractor
import com.sokolov.covboy.coverage.runner.error.SamplerCrashInfo
import com.sokolov.covboy.coverage.sampler.CoverageSampler
import com.sokolov.covboy.coverage.sampler.impl.GroupingModelsCoverageSampler
import com.sokolov.covboy.utils.KBoolExpr
import com.sokolov.covboy.utils.solverBuilder
import com.sokolov.covboy.utils.solverName
import org.ksmt.KContext
import org.ksmt.expr.KExpr
import org.ksmt.solver.KSolver
import org.ksmt.solver.z3.KZ3SMTLibParser
import org.ksmt.sort.KBoolSort
import java.io.File
import kotlin.reflect.KClass

fun main(args: Array<String>) {
    // args[0] - solver
    // args[1] - input
    // args[2] - outputFile

    val solverName = args[0]
    val inputFile = File(args[1])
    val outputFile = File(args[2])
    val errorFile = File(outputFile.parent, outputFile.nameWithoutExtension + "-error.json")

    val ctx = KContext()
    val solver = solverBuilder(solverName)(ctx)

    collectCoverageAndDump(ctx, solver, inputFile, outputFile, errorFile)
}

fun collectCoverage(ctx: KContext, solver: KSolver<*>, inputFile: File): FormulaCoverage<KBoolExpr, KBoolSort> {
    val expressions = KZ3SMTLibParser(ctx).parse(inputFile.toPath()).onEach(solver::assert)

    val predicates = BoolPredicatesExtractor(ctx).extractPredicates(expressions)

    return GroupingModelsCoverageSampler(solver, ctx, predicates.map { it.expr },
        { expr: KExpr<KBoolSort> -> predicates.first { it.expr == expr } }).computeCoverage()
}

fun collectCoverageAndDump(ctx: KContext, solver: KSolver<*>, inputFile: File, outputFile: File, errorFile: File) {
    try {
        outputFile.parentFile.mkdirs()

        val coverage = collectCoverage(ctx, solver, inputFile)
        coverage.dumpToFile(ctx, outputFile)
    } catch (e: Exception) {
        SamplerCrashInfo(SamplerCrashInfo.Reasons.EXCEPTION, e.toString()).writeToFile(errorFile)
    } finally {
        solver.close()
        ctx.close()
    }
}

fun <T : CoverageSampler<*>> makeMainArgs(
    solverClass: KClass<out KSolver<*>>,
    input: File,
    output: File,
    coverageSampler: KClass<out T>
): List<String> = listOf(
    solverClass.solverName,
    input.absolutePath,
    output.absolutePath,
    coverageSampler.simpleName!!
)

fun <T : CoverageSampler<*>> makeMainArgs(
    solverName: String,
    input: File,
    output: File,
    coverageSampler: KClass<out T>
): List<String> = listOf(
    solverName,
    input.absolutePath,
    output.absolutePath,
    coverageSampler.simpleName!!
)
