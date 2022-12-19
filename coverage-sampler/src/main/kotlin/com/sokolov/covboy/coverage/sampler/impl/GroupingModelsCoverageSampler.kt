package com.sokolov.covboy.coverage.sampler.impl

import com.sokolov.covboy.coverage.predicate.CoveragePredicate
import com.sokolov.covboy.coverage.predicate.bool.BoolPredicatesExtractor
import com.sokolov.covboy.coverage.sampler.CoverageSampler
import com.sokolov.covboy.utils.KBoolExpr
import com.sokolov.covboy.utils.evalOrNull
import com.sokolov.covboy.utils.solverName
import org.ksmt.KContext
import org.ksmt.expr.KEqExpr
import org.ksmt.expr.KExpr
import org.ksmt.expr.KNotExpr
import org.ksmt.solver.KModel
import org.ksmt.solver.KSolver
import org.ksmt.solver.KSolverStatus
import org.ksmt.sort.KBoolSort
import org.ksmt.sort.KSort

class GroupingModelsCoverageSampler<T : KSort>(
    solver: KSolver<*>,
    ctx: KContext,
    exprPredicates: Collection<KExpr<T>>,
    exprToPredicate: (KExpr<T>) -> CoveragePredicate<KExpr<T>, T>,
    val groupSize: Int = 2
) : CoverageSampler<T>(solver, ctx, exprPredicates, exprToPredicate) {

    init {
        check(groupSize > 0)
    }

    private var coverageIncreasedOnLastIteration: Boolean = true

    private val intersectionTracks = mutableSetOf<KBoolExpr>()
    private val intersectionAssumptions = mutableSetOf<KBoolExpr>()

    private var assumptionId: Long = 0
        get() = field++

    private val trackToExpr = mutableMapOf<KBoolExpr, KExpr<*>>()
    private val assumptionToTrack = mutableMapOf<KBoolExpr, KBoolExpr>()

    override fun cover() {
        with(ctx) {
            do {
                if (!coverageIncreasedOnLastIteration) {
                    /*
                optimization to push uncovered values for sampler
                 */

                    coverageIncreasedOnLastIteration = false

                    val uncoveredAssignments = anyUncoveredAssignments
                    val pushTrack = prover.assertAndTrack(mkOr(uncoveredAssignments))

                    when (prover.checkWithAssumptions(listOf(pushTrack))) {
                        KSolverStatus.SAT -> {
                            /*
                            optimization successful. all uncovered predicates pushed at once and covered here
                             */

                            val coverageIncreased = coverModel(prover.model()).isNotEmpty()
                            coverageIncreasedOnLastIteration = coverageIncreasedOnLastIteration || coverageIncreased
                        }

                        KSolverStatus.UNSAT -> {
                            /*
                            no one pushed assignment is sat
                             */

                            uncoveredAssignments.forEach {
                                onUnsatAssignment(it)
                            }
                        }

                        KSolverStatus.UNKNOWN -> throw IllegalStateException("Unknown result of formula: ${prover.reasonOfUnknown()}")
                    }

                } else {
                    coverageIncreasedOnLastIteration = false

                    val models = takeModels(groupSize)

                    models.forEach { model ->
                        val notEmptyModelCoverage = coverModel(model).isNotEmpty()
                        coverageIncreasedOnLastIteration = coverageIncreasedOnLastIteration || notEmptyModelCoverage
                    }

                    if (models.size < groupSize) {
                        /*
                    coverage collected, no models left
                         */

                        break
                    }

                    val getModelAssignmentsOnPredicates = { model: KModel ->
                        predicates.keys.map { it to model.evalOrNull(it) }
                            .mapNotNull { if (it.second == null) null else it.first eq it.second!! }
                            .toSet()
                    }

                    val intersection = models.fold(getModelAssignmentsOnPredicates(models.first())) { acc, currModel ->
                        acc.intersect(getModelAssignmentsOnPredicates(currModel))
                    }

                    if (intersection.isEmpty()) {
                        continue
                    }

                    val tracks = intersection.map {
                        val ass = ctx.mkConst("is_ass:$assumptionId", ctx.boolSort)
                        intersectionAssumptions += ass

                        val assumedIntersection = ass implies !it

                        prover.assertAndTrack(assumedIntersection).apply {
                            trackToExpr[this] = !it
                            assumptionToTrack[ass] = this
                        }
                    }
                    intersectionTracks.addAll(tracks)

                    if (prover.checkWithAssumptions(intersectionAssumptions.toList()) == KSolverStatus.UNSAT) {
                        resolveConflict(intersectionAssumptions.toList())
                    }
                }


            } while (!isCovered)

        }
    }

    private fun resolveConflict(assumptions: List<KBoolExpr>) {
        val unsatCore = prover.unsatCore().toSet()

        if (unsatCore.size > (assumptions + intersectionTracks).count { it in unsatCore }) {
            /*
            conflict between intersection negations
             */

            unsatCore.filter { it in intersectionAssumptions }.forEach(::clearAssumption)
        } else {
            /*
             conflict between original formula and the [tracks]
             unsat core == tracks
             */

            if (assumptions.containsAll(unsatCore - intersectionTracks)) {

                var allTracksByOneAreSat = true

                assumptions.forEach { assumption ->


                    when (prover.checkWithAssumptions(listOf(assumption))) {
                        KSolverStatus.SAT -> allTracksByOneAreSat = allTracksByOneAreSat && true
                        KSolverStatus.UNSAT -> {
                            val track = assumptionToTrack[assumption]
                            val expr = trackToExpr[track]

                            println("expr: $expr")
                            when {
//                                expr is KEqExpr<*> && expr.lhs in predicates.keys -> predicates[expr.lhs]!!.fixUnsatValue(expr.rhs as KExpr<T>)
                                expr is KNotExpr && expr.arg is KEqExpr<*> && (expr.arg as KEqExpr<*>).lhs in predicates.keys -> predicates[(expr.arg as KEqExpr<*>).lhs]!!.fixNoMoreSatValues()
                            }


                            clearAssumption(assumption)

                            allTracksByOneAreSat = false
                        }

                        KSolverStatus.UNKNOWN -> throw IllegalStateException("Formula is unknown: ${prover.reasonOfUnknown()}")
                    }
                }

                if (allTracksByOneAreSat) {
                    unsatCore.filter { it in intersectionAssumptions }.forEach(::clearAssumption)
                }
            }

        }
    }

    private fun clearAssumption(assumption: KBoolExpr) {
        intersectionAssumptions -= assumption
        clearTrack(assumptionToTrack[assumption]!!)
        assumptionToTrack -= assumption
    }

    private fun clearTrack(track: KBoolExpr) {
        intersectionTracks -= track
        trackToExpr -= track
    }

    override fun toString(): String = "GroupingModelsCoverage[${prover.solverName}, groupSize=$groupSize]"
}

fun makeBoolGroupingModelsCoverageSampler(
    ctx: KContext,
    solver: KSolver<*>,
    assertions: List<KBoolExpr>,
    groupSize: Int
): GroupingModelsCoverageSampler<KBoolSort> {
    val predicates = BoolPredicatesExtractor(ctx).extractPredicates(assertions)

    return GroupingModelsCoverageSampler(
        solver, ctx, predicates.map { it.expr },
        { expr: KExpr<KBoolSort> -> predicates.first { it.expr == expr } }, groupSize
    )
}