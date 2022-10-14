package com.microsoft.z3.coverage

import com.sokolov.covboy.coverage.AtomCoverageBase
import com.sokolov.covboy.coverage.CoverageSampler
import com.sokolov.covboy.logger
import com.sokolov.covboy.prover.Assignment
import com.sokolov.covboy.prover.BaseProverEnvironment
import com.sokolov.covboy.prover.model.ModelAssignments
import org.sosy_lab.java_smt.api.BooleanFormula

class ModelsEnumerationCoverage(
    prover: BaseProverEnvironment,
    coveragePredicates: Collection<BooleanFormula>
) : CoverageSampler(prover, coveragePredicates) {

    override fun computeCoverage(
        coverModel: (ModelAssignments<BooleanFormula>) -> Set<AtomCoverageBase>,
        coverAtom: (assignment: Assignment<BooleanFormula>) -> AtomCoverageBase,
        onImpossibleAssignmentFound: (assignment: Assignment<BooleanFormula>) -> Unit
    ) {
        prover.push()
        modelsEnumerator.forEach(coveragePredicates) {
            coverModel(it)
        }
        prover.pop()
        logger().info("Traversed ${modelsEnumerator.traversedModelsCount} models")
    }
}