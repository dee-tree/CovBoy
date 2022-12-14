package com.microsoft.z3.coverage.unitpropogation


/*
class UnitPropagationBasedCoverageSampler(
    prover: BaseProverEnvironment,
    coveragePredicates: Collection<BooleanFormula>
) : CoverageSampler(prover, coveragePredicates) {

    override fun computeCoverage(
        coverModel: (ModelAssignments<BooleanFormula>) -> Set<AtomCoverageBase>,
        coverAtom: (assignment: Assignment<BooleanFormula>) -> AtomCoverageBase,
        onImpossibleAssignmentFound: (assignment: Assignment<BooleanFormula>) -> Unit
    ) {
        while (!isCovered) {

            val assertion = uncoveredAtomsWithAnyValue.first().let { expr ->
                customAssertionsStorage.assert(expr.asExpr(), false)
            }

            logger().trace("Propagate unit: $assertion")

            when (checkWithAssumptions()) {
                Status.SATISFIABLE -> {
                    coverModel(solver.model)
                }

                Status.UNSATISFIABLE -> {
                    val (atom, value) = assertion.let {
                        if (it.expr.isNot) !it.expr to context.mkFalse() else it.expr to context.mkTrue()
                    }
                    onImpossibleAssignmentFound(Assignment(atom, value))
                }
                Status.UNKNOWN -> throw IllegalStateException("Unknown result of check")
            }
            assertion.disable()

        }
    }
}
*/
