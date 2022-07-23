package com.sokolov.z3cov

import com.microsoft.z3.deepestBoolExprs
import com.microsoft.z3.solver
import com.microsoft.z3.withContext

fun main() {

    withContext {
        with(solver()) {
            fromFile("input/3104.smt2")


            println(check())

            deepestBoolExprs {
                println("deepest one: ${it}")
            }

        }
    }

}