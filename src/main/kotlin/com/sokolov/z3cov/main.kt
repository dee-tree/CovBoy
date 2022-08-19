package com.sokolov.z3cov

import com.microsoft.z3.*
import com.microsoft.z3.coverage.CoverageSampling

fun main() {

    withContext {
        with(solver()) {
//            fromFile("input/2478.smt2")
//            fromFile("input/abs.smt2")
            fromFile("input/boolean_simple.smt2")
//            fromFile("input/3137.smt2")


            println(check())

            CoverageSampling(this, this@withContext).enumerate()

        }
    }

}