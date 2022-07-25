package com.sokolov.z3cov

import com.microsoft.z3.*
import java.util.TreeMap
import java.util.TreeSet

fun main() {

    withContext {
        with(solver()) {
//            fromFile("input/3104.smt2")
//            fromFile("input/abs.smt2")
            fromFile("input/boolean_simple.smt2")
//            fromFile("input/2520.smt2")


            println(check())

            val constraints = mutableListOf<List<Pair<BoolExpr, Expr>>>()

            whileSat {
                push(); check()
                println("Found new constraints...")
                val currentConstraints = deepestBoolExprs().map { it to model.eval(it, true) }
                constraints.add(currentConstraints)

                add(nand(*(currentConstraints.filter { it.second == mkTrue() }.map { it.first } + currentConstraints.filter { it.second == mkFalse() }.map { !it.first }).toTypedArray()))
                if(check() == Status.UNSATISFIABLE) {
                    pop()
                    add(or(*(currentConstraints.filter { it.second == mkTrue() }.map { !it.first } + currentConstraints.filter { it.second == mkFalse() }.map { it.first }).toTypedArray()))
                    println("on unsat: ${check()}")
                }

            }

            // constraints print
            println("all constraints:")
            constraints.forEachIndexed { index, constraint ->
                println("$index\t\n${constraint.joinToString("\n") { "\t\t*\t" + it }}")
            }

            val c = constraints[0].first { it.second == mkTrue() }
            constraints.filter { c in it }.also { println(it) }
//            constraints.permutations().also { println(it.joinToString("\n")) }

        }
    }

}

fun <V> List<V>.permutations(): List<List<V>> {
    val retVal: MutableList<List<V>> = mutableListOf()

    fun <V> MutableList<V>.swap(aIdx: Int, bIdx: Int) {
        val cache = this[aIdx]
        this[aIdx] = this[bIdx]
        this[bIdx] = cache
    }

    fun generate(k: Int, list: MutableList<V>) {
        // If only 1 element, just output the array
        if (k == 1) {
            retVal.add(list.toList())
        } else {
            for (i in 0 until k) {
                generate(k - 1, list)
                if (k % 2 == 0) {
                    list.swap(i, k - 1)
                } else {
                    list.swap(0, k - 1)
                }
            }
        }
    }

    generate(this.count(), this.toMutableList())
    return retVal
}