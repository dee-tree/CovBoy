package com.sokolov.z3cov

import com.microsoft.z3.*

fun main() {

    withContext {
        with(solver()) {
//            fromFile("input/3104.smt2")
//            fromFile("input/abs.smt2")
            fromFile("input/boolean_simple.smt2")
//            fromFile("input/3133.smt2")


            println(check())

            val constraints = mutableListOf<List<Pair<BoolExpr, Expr>>>()

            val deepestBoolExprs = deepestBoolExprs()

            val models = enumerateModels()

            do {
                val pair = models.take(2).toList()
                if (pair.count() < 2) break

                val intersection = pair
                    .fold(
                        deepestBoolExprs.map { it to pair.first().eval(it, true) }.toSet()
                    ) { acc, current -> acc.intersect(deepestBoolExprs.map { it to current.eval(it, true) }.toSet()) }
                println("intersection: ${intersection}")

                val intersectionConstraint = nand(
                    *(intersection.filter { it.second == mkTrue() }
                        .map { it.first }
                            + intersection.filter { it.second == mkFalse() }
                        .map { !it.first }).toTypedArray()
                )
                assertAndTrack(intersectionConstraint, mkBoolConst(intersectionConstraint.toString()))


            } while (true)

            println(unsatCore.contentToString())

            // constraints print
            println("all constraints:")
            constraints.forEachIndexed { index, constraint ->
                println("$index\t\n${constraint.joinToString("\n") { "\t\t*\t" + it }}")
            }

            constraints.forEachIndexed { i, constraint ->
                for (j in (i + 1)..constraints.lastIndex) {
                    println(constraints[i].toSet().intersect(constraints[j]).filter { it.second == mkTrue() })
                }
            }


        }
    }

}