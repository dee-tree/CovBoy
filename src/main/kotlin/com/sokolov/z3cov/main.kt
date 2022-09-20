package com.sokolov.z3cov

import com.microsoft.z3.coverage.intersections.ModelsIntersectionCoverage
import com.microsoft.z3.solver
import com.microsoft.z3.withContext

fun main() {

    repeat(10) {
        withContext {
            with(solver(true, 12345)) {
//            fromFile("input/abs.smt2")
//            fromFile("input/boolean_simple.smt2")
                fromFile("input/3190.smt2")
//            fromFile("input/bench.smt2")

//            println(check())
                ModelsIntersectionCoverage(this, this@withContext).computeCoverage().asStringInfo().also { println(it) }
            }
        }
    }

}