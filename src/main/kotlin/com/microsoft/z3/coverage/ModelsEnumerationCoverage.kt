package com.microsoft.z3.coverage

import com.microsoft.z3.*

class ModelsEnumerationCoverage(solver: Solver, context: Context) : CoverageSampler(solver, context) {

    private val modelsEnumerator = ModelsEnumerator(
        solver = solver,
        context = context,
        assertionsStorage = customAssertionsStorage,
        check = ::checkWithAssumptions
    )

    override fun computeCoveringModels(): Collection<Model> {
        return modelsEnumerator.all()
    }
}