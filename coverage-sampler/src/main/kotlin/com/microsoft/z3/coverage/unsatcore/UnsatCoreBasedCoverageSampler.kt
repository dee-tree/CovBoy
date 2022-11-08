package com.microsoft.z3.coverage.unsatcore

import com.sokolov.covboy.coverage.AtomCoverageBase
import com.sokolov.covboy.coverage.CoverageSampler
import com.sokolov.covboy.coverage.EmptyAtomCoverage
import com.sokolov.covboy.logger
import com.sokolov.covboy.prover.Assignment
import com.sokolov.covboy.prover.BaseProverEnvironment
import com.sokolov.covboy.prover.Status
import com.sokolov.covboy.prover.model.BoolModelAssignmentsImpl
import com.sokolov.covboy.prover.model.ModelAssignments
import com.sokolov.covboy.smt.isNot
import com.sokolov.covboy.smt.nand
import com.sokolov.covboy.smt.notOptimized
import org.sosy_lab.java_smt.api.BooleanFormula


class UnsatCoreBasedCoverageSampler(
    prover: BaseProverEnvironment,
    coveragePredicates: Collection<BooleanFormula>
) : CoverageSampler(prover, coveragePredicates) {

    override fun computeCoverage(
        coverModel: (ModelAssignments<BooleanFormula>) -> Set<AtomCoverageBase>,
        coverAtom: (assignment: Assignment<BooleanFormula>) -> AtomCoverageBase,
        onImpossibleAssignmentFound: (assignment: Assignment<BooleanFormula>) -> Unit
    ) {
//        val initialUncoveredValues = uncoveredValuesCount
        while (!isCovered) {
//            logger().trace("Remain uncovered values: $uncoveredValuesCount / $initialUncoveredValues")
            val assertions = buildList {
                uncoveredAtomsWithAnyValue.first().also/*forEach*/ { expr ->
                    if (expr.asExpr(prover) !in prover.formulas)
                        prover.addConstraint(expr.asExpr(prover), true, "uc.uncovered.atom").also { add(it) }
                }
            }

            when (prover.check()) {
                Status.SAT -> {
                    val model = BoolModelAssignmentsImpl(prover.model, coveragePredicates, prover)

                    coverModel(model)//.also { println("covered atoms: " + it.filter { it !is EmptyAtomCoverage }.size) }
                }

                Status.UNSAT -> {
                    do {
                        backtrackUnsatCore(onImpossibleAssignmentFound)

                        if (assertions.any { it in prover.enabledSwitchableConstraints } && prover.check() == Status.SAT) {
                            coverModel(
                                BoolModelAssignmentsImpl(
                                    prover.model,
                                    coveragePredicates,
                                    prover
                                )
                            )//.also { println("after backtracking covered atoms: " + it.filter { it !is EmptyAtomCoverage }.size) }
                        }
                    } while (prover.check() == Status.UNSAT)
                    coverModel(BoolModelAssignmentsImpl(prover.model, coveragePredicates, prover))
                }
                Status.UNKNOWN -> throw IllegalStateException("Unknown result of check")
            }

            assertions.filter { it in prover.enabledSwitchableConstraints }.forEach(prover::disableConstraint)
        }
    }

    private fun backtrackUnsatCore(onImpossibleAssignmentFound: (assignment: Assignment<BooleanFormula>) -> Unit) {
        val unsatCore = prover.unsatCore

        val ucAssertions = prover.filterSwitchableConstraints { it.original in unsatCore }

        if (ucAssertions.size == 1) {
            //logger().trace("ucAssertions.size == 1")
            val (atom, value) = ucAssertions.first().let {
                if (formulaManager.booleanFormulaManager.isNot(it))
                    prover.fm.booleanFormulaManager.notOptimized(it) to formulaManager.booleanFormulaManager.makeFalse()
                else it to formulaManager.booleanFormulaManager.makeTrue()
            }
            onImpossibleAssignmentFound(Assignment(atom, value))
        }

        if (ucAssertions.isNotEmpty()) {
            val ucAssertionsNand = prover.fm.booleanFormulaManager.nand(ucAssertions)

            if (ucAssertionsNand !in prover.formulas) {
//                prover.addConstraint(ucAssertionsNand, true, "uc.backtrack")
                prover.addConstraint(ucAssertionsNand, false, "uc.backtrack")
            }

            ucAssertions.forEach(prover::disableConstraint)
        }
    }
}
