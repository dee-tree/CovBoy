package com.microsoft.z3.coverage.unsatcore

/*
class UnsatCoreBasedCoverageSampler(solver: Solver, context: Context) : CoverageSampler(solver, context) {

    override fun computeCoverage(
        coverModel: (Model) -> Set<AtomCoverageBase>,
        coverAtom: (atom: BoolExpr, value: BoolExpr) -> AtomCoverageBase,
        onImpossibleAssignmentFound: (assignment: Assignment<BoolExpr>) -> Unit
    ) {
        while (!isCovered) {

            val assertions = buildList {
                uncoveredAtomsWithAnyValue.first().let { expr ->
                    customAssertionsStorage.assert(expr.asExpr(), false).also { add(it) }
                }
            }

            when (checkWithAssumptions()) {
                Status.SATISFIABLE -> {
                    coverModel(solver.model).also { println("covered atoms: " + it.filter { it !is EmptyAtomCoverage }.size) }
                }

                Status.UNSATISFIABLE -> {
                    do {
                        backtrackUnsatCore(onImpossibleAssignmentFound)
                        if (assertions.any { it.enabled }) {
                            if (checkWithAssumptions() == Status.SATISFIABLE) {
                                coverModel(solver.model).also { println("after backtracking covered atoms: " + it.filter { it !is EmptyAtomCoverage }.size) }
                            }
                        }
                    } while (checkWithAssumptions() == Status.UNSATISFIABLE)
                    coverModel(solver.model)
                }
                Status.UNKNOWN -> throw IllegalStateException("Unknown result of check")
            }

            assertions.filter { it.enabled }.forEach(Assertion::disable)
        }
    }

    private fun backtrackUnsatCore(onImpossibleAssignmentFound: (assignment: Assignment<BoolExpr>) -> Unit) {
        val unsatCore = solver.unsatCore

        val ucAssertions = customAssertionsStorage.assertions.filter { it.uidExpr in unsatCore }

        if (ucAssertions.size == 1) {
            val (atom, value) = ucAssertions.first().let { if (it.expr.isNot) !it.expr to context.mkFalse() else it.expr to context.mkTrue() }
            onImpossibleAssignmentFound(Assignment(atom, value))
        }

        customAssertionsStorage.assert(context.nand(*ucAssertions.map { it.expr }.toTypedArray()), true)

        ucAssertions.forEach(Assertion::disable)
    }
}*/
