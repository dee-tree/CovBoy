package com.microsoft.z3.coverage

import com.sokolov.covboy.coverage.AtomCoverageBase
import com.sokolov.covboy.coverage.CoverageSampler
import com.sokolov.covboy.logger
import com.sokolov.covboy.prover.Assignment
import com.sokolov.covboy.prover.IProver
import org.sosy_lab.java_smt.api.BooleanFormula
import org.sosy_lab.java_smt.api.Model

class ModelsEnumerationCoverage(
    prover: IProver,
    coveragePredicates: Collection<BooleanFormula>
) : CoverageSampler(prover, coveragePredicates) {

    override fun computeCoverage(
        coverModel: (Model) -> Set<AtomCoverageBase>,
        coverAtom: (assignment: Assignment<BooleanFormula>) -> AtomCoverageBase,
        onImpossibleAssignmentFound: (assignment: Assignment<BooleanFormula>) -> Unit
    ) {
        prover.push()
        modelsEnumerator.forEach {
            coverModel(it)
        }
        prover.pop()
        logger().info("Traversed ${modelsEnumerator.traversedModelsCount} models")
    }
}