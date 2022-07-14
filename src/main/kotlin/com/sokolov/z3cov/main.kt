package com.sokolov.z3cov

import com.microsoft.z3.*

/*

обход моделей конкретно по предикатам
1 модель - 1 предикат


 */

fun main() {

    withContext {
        with(solver()) {

            fromString("""
                (declare-const a Int)
                (declare-const b Int)
                (assert (= (+ a b) 10))
            """.trimIndent())

//            val ai = mkIntConst("ai")
//            val c = mkInt(10).isConst
//            val bi = mkIntConst("bi")
//
//            val f = mkAdd(ai, bi) eq  mkInt(10)

/*            val abv = mkBVConst("abv", 2)
            val bbv = mkBVConst("bbv", 2)


            val a = boolConst("a")
            val b = boolConst("b")
            val c = boolConst("c")
            val d = boolConst("d")*/
//            val f = ((a or b or c) and d) //or (a and !a)
//            val f = mkBVSLE(abv, bbv) and mkBVSGE(abv, bbv)


//            add(f)

            println(allModels().joinToString("\n\n"))

        }
    }

}