package com.microsoft.z3.coverage

/*
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
}*/
