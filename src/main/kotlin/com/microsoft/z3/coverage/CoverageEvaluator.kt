package com.microsoft.z3.coverage

import com.microsoft.z3.*

class CoverageEvaluator(
    solver: Solver,
    context: Context
) {

    private val atoms = solver.atoms

    private val uncoveredValues = mutableMapOf<BoolExpr, MutableSet<BoolExpr>>()

    val isCovered: Boolean
        get() = uncoveredValues.all { it.value.isEmpty() }

    init {
        atoms.forEach { atom ->
            uncoveredValues[atom] = mutableSetOf(context.mkTrue(), context.mkFalse())
        }
    }

    fun cover(model: Model) {
        atoms.forEach { atom ->
            coverAtom(atom, model.eval(atom, false) as BoolExpr)
        }
    }

    fun coverAtom(atom: BoolExpr, value: BoolExpr) {
        uncoveredValues[atom]?.remove(value) ?: return

        if (uncoveredValues[atom]?.isEmpty() == true) {
            uncoveredValues.remove(atom)
        }
    }

    fun eval(models: Collection<Model>): Map<BoolExpr, Double> {
        val coverage = buildMap<BoolExpr, MutableSet<BoolExpr>> {
            atoms.forEach { atom ->
                put(atom, mutableSetOf())
            }
        }

        models.forEach { model ->
            atoms.forEach { atom ->
                coverage[atom]?.add(model.eval(atom, false) as BoolExpr)
            }
        }
        println("coverage values: ${coverage}")

        return coverage.entries.associate { it.key to it.value.size / 2.0 }
    }

}