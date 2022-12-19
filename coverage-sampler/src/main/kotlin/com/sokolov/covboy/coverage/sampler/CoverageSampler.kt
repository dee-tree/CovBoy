package com.sokolov.covboy.coverage.sampler

import com.sokolov.covboy.coverage.FormulaCoverage
import com.sokolov.covboy.coverage.predicate.CoveragePredicate
import com.sokolov.covboy.coverage.toCoverage
import com.sokolov.covboy.utils.evalOrNull
import com.sokolov.covboy.utils.logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ksmt.KContext
import org.ksmt.expr.KEqExpr
import org.ksmt.expr.KExpr
import org.ksmt.solver.KModel
import org.ksmt.solver.KSolver
import org.ksmt.solver.KSolverStatus
import org.ksmt.sort.KSort

abstract class CoverageSampler<T : KSort> constructor(
    protected val prover: KSolver<*>,
    protected val ctx: KContext,
    exprPredicates: Collection<KExpr<T>>,
    exprToPredicate: (KExpr<T>) -> CoveragePredicate<KExpr<T>, T>
) : AutoCloseable {

    protected val predicates = buildMap {
        exprPredicates.forEach { expr ->
            put(expr, exprToPredicate(expr).toMutable())
        }
    }

    protected val uncoveredValuesCount: Int
        get() = predicates.values.count { !it.isCovered }

    protected abstract fun cover()

    fun computeCoverage(): FormulaCoverage<KExpr<T>, T> {
        prover.push()

        val checkStatus = prover.check()
        if (checkStatus != KSolverStatus.SAT) {
            System.err.println("Formula is $checkStatus. No coverage is available!")
            throw IllegalStateException("Formula is $checkStatus. No coverage is available!")
        }

        val initialUncoveredValues = uncoveredValuesCount
        val progressPrinter = GlobalScope.launch {
            while (true) {
                logger().trace("Remain uncovered values: $uncoveredValuesCount / $initialUncoveredValues")
                delay(1000)
            }
        }

        cover()
        progressPrinter.cancel(CancellationException("Coverage collected"))
        prover.pop()
        return predicates.values.map { it.toImmutable() }.toCoverage()
    }

    protected val isCovered: Boolean
        get() = predicates.values.all { it.isCovered }

    protected val uncoveredPredicates: List<CoveragePredicate<KExpr<T>, T>>
        get() = predicates.values.filter { !it.isCovered }

    protected val anyUncoveredAssignments: List<KEqExpr<T>>
        get() = uncoveredPredicates.map { ctx.mkEq(it.expr, it.getAnyUncoveredValue()) }

    protected fun coverModel(model: KModel): List<KEqExpr<T>> = buildList {
        uncoveredPredicates
            .forEach {
                val value = model.evalOrNull(it.expr)
                if (value == null) {
                    /*
                     * cover all values like lazy-assignable to this expr
                     */
                    while (!it.isCovered) {
                        val value = it.getAnyUncoveredValue()
                        val exprToValue = ctx.mkEq(it.expr, value)
                        add(exprToValue)
                        coverPredicate(exprToValue)
                    }
                } else {
                    val exprToValue = ctx.mkEq(it.expr, value)
                    add(exprToValue)
                    coverPredicate(exprToValue)
                }
            }
    }

    /**
     * @return `true` if [KEqExpr.lhs] wasn't covered before the call
     */
    protected fun coverPredicate(assignment: KEqExpr<T>): Boolean =
        predicates[assignment.lhs]?.cover(assignment.rhs) ?: false

    protected fun onUnsatAssignment(assignment: KEqExpr<T>) {
        predicates[assignment.lhs]!!.fixUnsatValue(assignment.rhs)
    }

    fun takeModels(count: Int): List<KModel> = buildList {
        repeat(count) {
            if (prover.check() != KSolverStatus.SAT)
                return@buildList

            val model = prover.model().detach()
            add(model)

            with(ctx) {
                val constraints = predicates.keys.mapNotNull { predicate ->
                    val value = model.evalOrNull(predicate)
                    value?.let { predicate eq value }
                }
                prover.assert(!mkAnd(constraints))
            }

        }
    }

    override fun close() {
        predicates.values.forEach { predicate -> predicate.close() }
        prover.close()
        ctx.close()
    }

}
