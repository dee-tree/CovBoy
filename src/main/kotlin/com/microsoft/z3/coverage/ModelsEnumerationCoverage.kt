package com.microsoft.z3.coverage

import com.microsoft.z3.*
import com.sokolov.z3cov.logger

class ModelsEnumerationCoverage(solver: Solver, context: Context) : CoverageSampler(solver, context) {

    override fun computeCoverage(
        coverModel: (Model) -> Set<AtomCoverageBase>,
        coverAtom: (atom: BoolExpr, value: BoolExpr) -> AtomCoverageBase,
        onImpossibleAtomValueFound: (atom: BoolExpr, impossibleValue: BoolExpr) -> Unit
    ) {
        modelsEnumerator.forEach {
            coverModel(it)
        }

        logger().info("Traversed ${modelsEnumerator.traversedModelsCount} models")
    }
}