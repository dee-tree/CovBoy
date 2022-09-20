package com.microsoft.z3.coverage

import com.microsoft.z3.*
import com.sokolov.smt.sampling.logger

class ModelsEnumerationCoverage(solver: Solver, context: Context) : CoverageSampler(solver, context) {

    override fun computeCoverage(
        coverModel: (Model) -> Set<AtomCoverageBase>,
        coverAtom: (atom: BoolExpr, value: BoolExpr) -> AtomCoverageBase,
        onImpossibleAssignmentFound: (assignment: Assignment<BoolExpr>) -> Unit
    ) {
        logger().info("Traversed ${modelsEnumerator.traversedModelsCount} models")
    }
}