package com.sokolov.covboy.sampler

import com.sokolov.covboy.coverage.PredicatesCoverage
import com.sokolov.covboy.ensureSat
import com.sokolov.covboy.isCovered
import com.sokolov.covboy.sampler.params.CoverageSamplerParams
import com.sokolov.covboy.sampler.params.CoverageSamplerParamsBuilder
import org.ksmt.KContext
import org.ksmt.expr.KExpr
import org.ksmt.runner.generated.createInstance
import org.ksmt.runner.generated.models.SolverType
import org.ksmt.solver.KModel
import org.ksmt.solver.KSolverStatus
import org.ksmt.sort.KBoolSort
import org.ksmt.sort.KSort
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

abstract class CoverageSampler<T : KSort> constructor(
    private val solverType: SolverType,
    protected val ctx: KContext,
    val assertions: List<KExpr<KBoolSort>>,
    val coverageUniverse: Set<KExpr<T>>,
    val coveragePredicates: Set<KExpr<T>>,
    val completeModels: Boolean = DEFAULT_COMPLETE_MODELS,
    val solverTimeout: Duration = DEFAULT_SOLVER_TIMEOUT
) : AutoCloseable {

    protected val solver = solverType.createInstance(ctx)

    private val currentCoverageSat = HashMap<KExpr<T>, HashSet<KExpr<T>>>(coveragePredicates.size).apply {
        coveragePredicates.forEach { predicate ->
            put(predicate, hashSetOf())
        }
    }

    private val currentCoverageUnsat = HashMap<KExpr<T>, HashSet<KExpr<T>>>(coveragePredicates.size).apply {
        coveragePredicates.forEach { predicate ->
            put(predicate, hashSetOf())
        }
    }

    protected val KExpr<T>.coveredValues: Set<KExpr<T>>
        get() = currentCoverageSat.getValue(this) + currentCoverageUnsat.getValue(this)

    protected abstract fun coverFormula()

    fun computeCoverage(): PredicatesCoverage<T> {
        assertions.forEach(solver::assert)

        solver.ensureSat { "No coverage is available" }
        solver.push()

        coverFormula()

        solver.pop()

        return PredicatesCoverage(
            HashMap(currentCoverageSat),
            HashMap(currentCoverageUnsat),
            coverageUniverse,
            solverType
        )
    }

    protected val allPredicatesCovered: Boolean
        get() = coveragePredicates.all { it.isCovered }

    val KExpr<T>.isCovered: Boolean
        get() = isCovered(
            currentCoverageSat.getValue(this),
            currentCoverageUnsat.getValue(this),
            coverageUniverse
        )

    protected val uncoveredPredicates: List<KExpr<T>>
        get() = coveragePredicates.filter { !it.isCovered }

//    protected val anyUncoveredAssignments: List<KEqExpr<T>>
//        get() = uncoveredPredicates.map { ctx.mkEq(it.expr, it.getAnyUncoveredValue()) }

    protected fun coverModel(model: KModel): List<Pair<KExpr<T>, KExpr<T>>> = buildList {
        uncoveredPredicates
            .forEach { predicate ->
                val value = model.eval(predicate, isComplete = completeModels)

                if (!completeModels && value !in coverageUniverse) {
                    /*
                     * value of this term is not influence on formula satisfiability
                     */

                    (coverageUniverse - predicate.coveredValues).forEach {
                        add(predicate to it)
                        coverPredicateWithSatValue(predicate, it)
                    }
                } else {
                    /*
                     * this term has sat-value
                     */

                    add(predicate to value)
                    coverPredicateWithSatValue(predicate, value)
                }
            }
    }

    /**
     * @return `true` if [lhs] wasn't covered by [rhs] before the call
     */
    protected fun coverPredicateWithSatValue(lhs: KExpr<T>, rhs: KExpr<T>): Boolean {
        val exprUnsatValues = currentCoverageUnsat.getValue(lhs)

        if (lhs in exprUnsatValues) return false

        val exprSatValues = currentCoverageSat.getValue(lhs)
        return exprSatValues.add(rhs)
    }


    protected fun coverPredicateWithUnsatValue(lhs: KExpr<T>, rhs: KExpr<T>) {
        currentCoverageUnsat.getValue(lhs) += rhs
    }

    protected fun takeModels(count: Int): List<KModel> = buildList {
        repeat(count) {
            if (solver.check(solverTimeout) != KSolverStatus.SAT)
                return@buildList

            val model = solver.model().detach()
            add(model)

            with(ctx) {
                val constraints = coveragePredicates.mapNotNull { predicate ->
                    val value = model.eval(predicate, isComplete = completeModels)

                    if (value in coverageUniverse) {
                        predicate eq value

                    } else null
                }

                solver.assert(!mkAnd(constraints))
            }
        }
    }

    override fun close() {
        currentCoverageUnsat.clear()
        currentCoverageSat.clear()
        solver.close()
    }

    companion object {
        const val DEFAULT_COMPLETE_MODELS = true
        val DEFAULT_SOLVER_TIMEOUT = 1.seconds
    }

    object ParamKeys {
        const val SolverTimeoutMillis = "SolverTimeoutMillis"
        const val CompleteModels = "CompleteModels"
    }
}

fun CoverageSamplerParams.hasSolverTimeoutMillisParam(): Boolean =
    hasLongParam(CoverageSampler.ParamKeys.SolverTimeoutMillis)

fun CoverageSamplerParams.hasCompleteModelsParam(): Boolean = hasBoolParam(CoverageSampler.ParamKeys.CompleteModels)


fun CoverageSamplerParams.getSolverTimeoutMillisParam(): Long = getLong(CoverageSampler.ParamKeys.SolverTimeoutMillis)
fun CoverageSamplerParams.getCompleteModelsParam(): Boolean = getBool(CoverageSampler.ParamKeys.CompleteModels)

fun CoverageSamplerParamsBuilder.putSolverTimeoutMillis(value: Long) {
    putParam(CoverageSampler.ParamKeys.SolverTimeoutMillis, value)
}

fun CoverageSamplerParamsBuilder.putCompleteModels(value: Boolean) {
    putParam(CoverageSampler.ParamKeys.CompleteModels, value)
}
