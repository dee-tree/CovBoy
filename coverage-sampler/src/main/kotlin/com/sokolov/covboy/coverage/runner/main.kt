package com.sokolov.covboy.coverage.runner

import com.sokolov.covboy.coverage.predicate.bool.BoolPredicatesExtractor
import com.sokolov.covboy.coverage.sampler.impl.GroupingModelsCoverageSampler
import org.ksmt.KContext
import org.ksmt.expr.KExpr
import org.ksmt.solver.bitwuzla.KBitwuzlaSolver
import org.ksmt.solver.z3.KZ3SMTLibParser
import org.ksmt.sort.KBoolSort
import java.io.File

fun main(args: Array<String>) {
    val inputFile = File(args[0])
    val ctx = KContext()
    val solver = KBitwuzlaSolver(ctx)

    val exprs = KZ3SMTLibParser(ctx).parse(inputFile.toPath())

    exprs.forEach {
        solver.assert(it)
    }

    val predicates = BoolPredicatesExtractor(ctx).extractPredicates(exprs)

    val coverage = GroupingModelsCoverageSampler(solver, ctx, predicates.map { it.expr },
        { expr: KExpr<KBoolSort> -> predicates.first { it.expr == expr } }).computeCoverage()


    println("Coverage:")
    coverage.predicates.forEach {
        println("predicate ${it.expr} | values: ${it.satValues} | unsat values: ${it.unsatValues}")
    }
}