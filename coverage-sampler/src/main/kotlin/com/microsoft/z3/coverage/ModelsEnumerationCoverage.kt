package com.microsoft.z3.coverage

import com.sokolov.covboy.coverage.AtomCoverageBase
import com.sokolov.covboy.coverage.CoverageSampler
import com.sokolov.covboy.prover.IProver
import com.sokolov.covboy.logger
import com.sokolov.covboy.prover.Assignment
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.Model
import org.sosy_lab.java_smt.api.SolverContext

class ModelsEnumerationCoverage(
    context: SolverContext,
    prover: IProver,
    coveragePredicates: Collection<BooleanFormula>
) : CoverageSampler(context, prover, coveragePredicates) {

    override fun computeCoverage(
        coverModel: (Model) -> Set<AtomCoverageBase>,
        coverAtom: (assignment: Assignment<BooleanFormula>) -> AtomCoverageBase,
        onImpossibleAssignmentFound: (assignment: Assignment<BooleanFormula>) -> Unit
    ) {
        modelsEnumerator.forEach {
            coverModel(it)
        }

        logger().info("Traversed ${modelsEnumerator.traversedModelsCount} models")
    }
}