package com.sokolov.covboy.statistics

import org.ksmt.expr.KExpr
import org.ksmt.solver.KSolver
import org.ksmt.solver.KSolverConfiguration
import org.ksmt.solver.KSolverStatus
import org.ksmt.sort.KBoolSort
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

class KSolverExt<C : KSolverConfiguration>(
    private val delegate: KSolver<C>,
    private val onCheckSatMeasured: (KSolverStatus, Duration) -> Unit = { _, _ -> }
) : KSolver<C> by delegate {

    @OptIn(ExperimentalTime::class)
    override fun check(timeout: Duration): KSolverStatus = measureTimedValue { delegate.check(timeout) }
        .also { (checkSatStatus, duration) -> onCheckSatMeasured(checkSatStatus, duration) }
        .value

    @OptIn(ExperimentalTime::class)
    override fun checkWithAssumptions(assumptions: List<KExpr<KBoolSort>>, timeout: Duration): KSolverStatus {
        return measureTimedValue { delegate.checkWithAssumptions(assumptions, timeout) }
            .also { (checkSatStatus, duration) -> onCheckSatMeasured(checkSatStatus, duration) }
            .value
    }

}
