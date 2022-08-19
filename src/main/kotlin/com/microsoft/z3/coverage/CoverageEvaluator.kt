package com.microsoft.z3.coverage

import com.microsoft.z3.BoolExpr
import com.microsoft.z3.Model
import com.microsoft.z3.Solver
import com.microsoft.z3.atoms

class CoverageEvaluator(
    solver: Solver
) {

    private val atoms = solver.atoms

    fun eval(models: Collection<Model>): Map<BoolExpr, Double> {
        val coverage = buildMap<BoolExpr, MutableSet<BoolExpr>> {
            atoms.forEach { atom ->
                put(atom, mutableSetOf())
            }
        }

        models.forEach { model ->
            atoms.forEach { atom ->
                coverage[atom]?.add(model.eval(atom, true) as BoolExpr)
            }
        }

        return coverage.entries.associate { it.key to it.value.size / 2.0 }
    }

}