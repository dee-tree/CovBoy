package com.microsoft.z3.coverage

import com.microsoft.z3.*
import com.sokolov.z3cov.logger

class ModelsIntersectionCoverage(val intersectionSize: Int = 3, solver: Solver, context: Context) :
    CoverageSampler(solver, context) {

    init {
        val partialModelsParam = context.mkParams().apply { add("model.partial", true) }
        solver.setParameters(partialModelsParam)
    }

    private val modelsEnumerator = ModelsEnumerator(
        solver = solver,
        context = context,
        assertionsStorage = customAssertionsStorage,
        check = ::checkWithAssumptions
    )

    private val atoms = solver.atoms

    override fun computeCoveringModels(): Collection<Model> {
        val models = mutableSetOf<Model>()

        do {
            val currentBoundModels = modelsEnumerator.take(intersectionSize)
            models.addAll(currentBoundModels)

            currentBoundModels.forEach { coverage.cover(it) }

            if (currentBoundModels.count() < intersectionSize) break
            val intersection = currentBoundModels
                .fold(
                    atoms.map { it to currentBoundModels.first().eval(it, false) }.toSet()
                ) { acc, currentModel ->
                    acc.intersect(atoms.map { it to currentModel.eval(it, false) }.toSet())
                }

//            logger().info("intersection found: ${intersection}")

            if (intersection.isEmpty()) {
                logger().info("Found intersection, but it is empty")
                continue
            }
            logger().info("Found non-empty intersection consisting of ${intersection.size} atoms")

            val intersectionConstraint = intersection.connectWithAnd(context)
            val negatedIntersection = !intersectionConstraint
            logger().trace("Add constraint on negated intersection: $negatedIntersection")
            val negIntersectionAssertion = customAssertionsStorage.assert(negatedIntersection, false)


            // check assertions conflict
            if (checkWithAssumptions() == Status.UNSATISFIABLE) {
                logger().info("Assertions conflict found")
                val unsatCore = solver.unsatCore
                logger().trace("UnsatCore: ${unsatCore.contentToString()}")

                val customAssertionsFromCore = customAssertionsStorage.assertions.filter { it.uidExpr in unsatCore }

                logger().trace("custom assertions in unsat core: $customAssertionsFromCore")

                val customUids = customAssertionsFromCore.map { it.uidExpr }
                val customConditions = customAssertionsFromCore.map { it.assumption }

                // if only custom assertions conflict
                if (customUids.size != 1 && unsatCore.all { it in customUids || it in customConditions }) {
                    logger().info("Assertions conflict: custom assertions")
                    (customAssertionsFromCore - negIntersectionAssertion).filter { !it.isLocal }.forEach(Assertion::disable)
                } else {
                    // conflict with original assertions branch
                    logger().info("Assertions conflict: negated intersection with original formula")
                    logger().info("Disable negated intersection assertion")
                    negIntersectionAssertion.disable()
                    logger().trace("Negated intersection assertion disabled successfully: $negIntersectionAssertion")

                    if (customUids.size == 1) {
                        logger().info("Cover impossible value of atom!")
                        logger().trace("Cover impossible value of atom $intersectionConstraint is False")
                        coverage.coverAtom(intersectionConstraint, context.mkFalse())
                    }
                }
            }

        } while (!coverage.isCovered)

        return models
    }
}