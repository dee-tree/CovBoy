package com.microsoft.z3.coverage.unitpropogation

import com.microsoft.z3.*
import com.microsoft.z3.coverage.AtomCoverageBase
import com.microsoft.z3.coverage.CoverageSampler
import com.sokolov.z3cov.logger

class UnitPropagationBasedCoverageSampler(solver: Solver, context: Context) : CoverageSampler(solver, context) {

    override fun computeCoverage(
        coverModel: (Model) -> Set<AtomCoverageBase>,
        coverAtom: (atom: BoolExpr, value: BoolExpr) -> AtomCoverageBase,
        onImpossibleAssignmentFound: (assignment: Assignment<BoolExpr>) -> Unit
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