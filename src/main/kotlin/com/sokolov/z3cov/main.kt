package com.sokolov.z3cov

import com.microsoft.z3.coverage.ModelsEnumerationCoverage
import com.microsoft.z3.coverage.ModelsIntersectionCoverage
import com.microsoft.z3.solver
import com.microsoft.z3.withContext

fun main() {

    withContext {
        with(solver()) {
//            fromFile("input/abs.smt2")
//            fromFile("input/boolean_simple.smt2")
            fromFile("input/3190.smt2")


            println(check())

            ModelsEnumerationCoverage(this, this@withContext).printCoverage()
//            ModelsIntersectionCoverage(3, this, this@withContext).printCoverage()

        }
    }

}