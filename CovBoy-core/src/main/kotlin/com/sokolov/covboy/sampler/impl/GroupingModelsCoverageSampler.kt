package com.sokolov.covboy.sampler.impl

import com.sokolov.covboy.logger
import com.sokolov.covboy.sampler.*
import com.sokolov.covboy.sampler.params.CoverageSamplerParams
import com.sokolov.covboy.sampler.params.CoverageSamplerParamsBuilder
import org.ksmt.KContext
import org.ksmt.expr.KExpr
import org.ksmt.runner.generated.models.SolverType
import org.ksmt.solver.KSolverStatus
import org.ksmt.sort.KBoolSort
import org.ksmt.sort.KSort
import org.ksmt.utils.mkFreshConst
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Model1: a = true, b = true, c = false  |
 * Model2: a = true, b = true, d = true   |
 * ----------------------------------------
 * modelsConjunction = Model1 && Model2 (a = true, b = true)
 * assert(!modelsConjunction) // assert(a != true || b != true)
 *
 * ...
 * ...
 */
class GroupingModelsCoverageSampler<S : KSort>(
    solverType: SolverType,
    ctx: KContext,
    assertions: List<KExpr<KBoolSort>>,
    coverageUniverse: Set<KExpr<S>>,
    coveragePredicates: Set<KExpr<S>>,
    completeModels: Boolean = DEFAULT_COMPLETE_MODELS,
    solverTimeout: Duration = DEFAULT_SOLVER_TIMEOUT,
    val groupSize: Int = DEFAULT_MODELS_GROUP_SIZE
) : CoverageSampler<S>(
    solverType,
    ctx,
    assertions,
    coverageUniverse,
    coveragePredicates,
    completeModels,
    solverTimeout
) {

    init {
        check(groupSize > 0)
    }

    constructor(
        solverType: SolverType,
        ctx: KContext,
        assertions: List<KExpr<KBoolSort>>,
        coverageUniverse: Set<KExpr<S>>,
        coveragePredicates: Set<KExpr<S>>,
        params: CoverageSamplerParams
    ) : this(
        solverType,
        ctx,
        assertions,
        coverageUniverse,
        coveragePredicates,
        if (params.hasCompleteModelsParam()) params.getCompleteModelsParam() else DEFAULT_COMPLETE_MODELS,
        if (params.hasSolverTimeoutMillisParam()) params.getSolverTimeoutMillisParam().milliseconds else DEFAULT_SOLVER_TIMEOUT,
        if (params.hasModelsGroupSizeParam()) params.getModelsGroupSizeParam() else DEFAULT_MODELS_GROUP_SIZE
    )

    private val commonModelsTracks = mutableListOf<KExpr<KBoolSort>>()
    private val commonModelsAssumptions = mutableListOf<KExpr<KBoolSort>>()

    private val trackToExpr = mutableMapOf<KExpr<KBoolSort>, KExpr<*>>()
    private val assumptionToTrack = mutableMapOf<KExpr<KBoolSort>, KExpr<KBoolSort>>()

    override fun coverFormula() = with(ctx) {
        var iter = 0
        while (!allPredicatesCovered) {
            logger().trace("Iter ${iter++}")
            val thisStepUncoveredPredicates = uncoveredPredicates

            val modelsGroup = takeModels(groupSize, commonModelsAssumptions)
            val covered = modelsGroup.map { coverModel(it) }.also { logger().trace("Models: $it") }.flatten().toSet()

            logger().trace("Covered: $covered")

            if (covered.isEmpty()) {
                logger().trace("No covered predicates!")
            }

            if (modelsGroup.size < groupSize) {
                logger().trace("ModelsGroup size is: ${modelsGroup.size}, but required: $groupSize")
                /*
                 * no models left | Full coverage collected
                 */

                if (commonModelsAssumptions.isEmpty()) {
                    logger().trace("Break in modelgroup small")
                    break
                }

                logger().error("Reset assumptions")
                commonModelsAssumptions.reversed().forEach(::disableAssumption)
//                commonModelsAssumptions.forEach(::disableAssumption)

                continue
            }

            val commonModelAssignments = mutableListOf<KExpr<KBoolSort>>()

            thisStepUncoveredPredicates.forEach { predicate ->
                val predicateAssignments = covered.filter { it.first == predicate }
                if (predicateAssignments.size == 1) {
                    commonModelAssignments += ctx.mkEq(predicate, predicateAssignments[0].second)
                }
            }

            logger().trace("Models conj: $commonModelAssignments")

            if (commonModelAssignments.isEmpty())
                continue

            // TODO: think: conjunction or disjunction?
            val commonModelsConjunction = mkOr(commonModelAssignments)
            val commonModelsAssumption = boolSort.mkFreshConst("cm_track")
            val assumedCommonModelsConjunction = commonModelsAssumption implies commonModelsConjunction

            solver.assertAndTrack(assumedCommonModelsConjunction).also { commonModelsTrack ->
                commonModelsTracks += commonModelsTrack
                commonModelsAssumptions += commonModelsAssumption

                trackToExpr[commonModelsTrack] = assumedCommonModelsConjunction
                assumptionToTrack[commonModelsAssumption] = commonModelsTrack
            }

            solver.checkWithAssumptionsAndTimeout(commonModelsAssumptions).process()
        }

        // cover unsat-values explicitly
        uncoveredPredicates.forEach { predicate ->
            (coverageUniverse - predicate.coveredValues).forEach { unsatValue ->
                coverPredicateWithUnsatValue(predicate, unsatValue)
            }
        }
    }

    fun KSolverStatus.process() {
        when (this) {
            KSolverStatus.UNSAT -> {
                val unsatCore = solver.unsatCore().toHashSet()

                logger().error("Reset assumptions")
                for (i in commonModelsAssumptions.indices.reversed()) {
                    disableAssumption(commonModelsAssumptions[i])
                }
            }

            else -> { /* TODO: remove this clause */
            }
        }


    }


    private fun disableAssumption(assumption: KExpr<KBoolSort>) {
        commonModelsAssumptions -= assumption
        disableTrack(assumptionToTrack[assumption]!!)
        assumptionToTrack -= assumption
    }

    private fun disableTrack(track: KExpr<KBoolSort>) {
        commonModelsTracks -= track
        trackToExpr -= track
    }

    /*
    private var coverageIncreasedOnLastIteration: Boolean = true

    private val intersectionTracks = mutableSetOf<KExpr<KBoolSort>>()
    private val intersectionAssumptions = mutableSetOf<KExpr<KBoolSort>>()

    private var assumptionId: Long = 0
        get() = field++

    private val trackToExpr = mutableMapOf<KExpr<KBoolSort>, KExpr<*>>()
    private val assumptionToTrack = mutableMapOf<KExpr<KBoolSort>, KExpr<KBoolSort>>()

    override fun coverFormula() = with(ctx) {
            do {
                if (!coverageIncreasedOnLastIteration) {
                    *//*
                     *  optimization to push uncovered values for sampler
                     *//*

                    coverageIncreasedOnLastIteration = false

                    val uncoveredAssignments = anyUncoveredAssignments
                    val pushTrack = prover.assertAndTrack(mkOr(uncoveredAssignments))

                    when (prover.checkWithAssumptions(listOf(pushTrack))) {
                        KSolverStatus.SAT -> {
                            *//*
                            optimization successful. all uncovered predicates pushed at once and covered here
                             *//*

                            val coverageIncreased = coverModel(prover.model()).isNotEmpty()
                            coverageIncreasedOnLastIteration = coverageIncreasedOnLastIteration || coverageIncreased
                        }

                        KSolverStatus.UNSAT -> {
                            *//*
                            no one pushed assignment is sat
                             *//*

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
                        *//*
                    coverage collected, no models left
                         *//*

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

    private fun resolveConflict(assumptions: List<KBoolExpr>) {
        val unsatCore = prover.unsatCore().toSet()

        if (unsatCore.size > (assumptions + intersectionTracks).count { it in unsatCore }) {
            *//*
            conflict between intersection negations
             *//*

            unsatCore.filter { it in intersectionAssumptions }.forEach(::clearAssumption)
        } else {
            *//*
             conflict between original formula and the [tracks]
             unsat core == tracks
             *//*

            if (assumptions.containsAll(unsatCore - intersectionTracks)) {

                var allTracksByOneAreSat = true

                assumptions.forEach { assumption ->


                    when (prover.checkWithAssumptions(listOf(assumption))) {
                        KSolverStatus.SAT -> allTracksByOneAreSat = allTracksByOneAreSat && true
                        KSolverStatus.UNSAT -> {
                            val track = assumptionToTrack[assumption]
                            val expr = trackToExpr[track]

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
    )*/
    object ParamKeys {
        const val ModelsGroupSize = "ModelGroupSize"
    }

    companion object {
        const val DEFAULT_MODELS_GROUP_SIZE = 2
    }
}

fun CoverageSamplerParams.hasModelsGroupSizeParam(): Boolean =
    hasIntParam(GroupingModelsCoverageSampler.ParamKeys.ModelsGroupSize)

fun CoverageSamplerParams.getModelsGroupSizeParam(): Int =
    getInt(GroupingModelsCoverageSampler.ParamKeys.ModelsGroupSize)

fun CoverageSamplerParamsBuilder.putModelsGroupSizeParam(value: Int) {
    check(value > 0)
    putParam(GroupingModelsCoverageSampler.ParamKeys.ModelsGroupSize, value)
}
