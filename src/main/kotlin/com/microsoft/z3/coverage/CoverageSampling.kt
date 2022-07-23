package com.microsoft.z3.coverage

import com.microsoft.z3.*
import java.util.TreeMap

class CoverageSampling(private val solver: Solver) {

    private val exprSATs: TreeMap<BoolExpr, MutableList<Boolean>> = TreeMap()
    private val models: MutableList<Model> = mutableListOf()

    init {
        solver.walk { expr ->
            if (expr is BoolExpr) {
                exprSATs[expr] = mutableListOf()
            }

            true
        }
    }

    fun coveredSample() {
        var idx = 0

        exprSATs.keys.forEach { boolExpr ->
            solver.push()

            solver.add(boolExpr)
            val status = solver.check()

            if (status == Status.SATISFIABLE) {
                models.add(/*idx, */solver.model)

                solver.walk { expr ->
                    exprSATs[expr]?.add(/*idx, */solver.check(expr, *models.last().constDeclsAsExpressions.toTypedArray()) == Status.SATISFIABLE)
                    true
                }
            }



            solver.pop()
            idx++
        }

        println(exprSATs.entries.map { "${it.key}: ${it.value.zip(models.map { it.constDeclsAsExpressions })}" }.joinToString("\n"))
    }
}
