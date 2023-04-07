package com.sokolov.covboy

import org.ksmt.expr.KExpr
import org.ksmt.solver.KSolver
import org.ksmt.solver.KSolverConfiguration
import org.ksmt.solver.KSolverStatus
import org.ksmt.sort.KBoolSort
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class KSolverExt<C : KSolverConfiguration>(
    private val delegate: KSolver<C>,
    private val onCheckSatMeasured: (KSolverStatus, Duration) -> Unit = { _, _ -> }
) : KSolver<C> by delegate {

    @ExperimentalTime
    override fun check(timeout: Duration): KSolverStatus {
        val result: KSolverStatus
        val checkSatDuration = measureTime {
            result = delegate.check(timeout)
        }

        onCheckSatMeasured(result, checkSatDuration)
        return result
    }

    @ExperimentalTime
    override fun checkWithAssumptions(assumptions: List<KExpr<KBoolSort>>, timeout: Duration): KSolverStatus {
        val result: KSolverStatus
        val checkSatDuration = measureTime {
            result = delegate.checkWithAssumptions(assumptions, timeout)
        }

        onCheckSatMeasured(result, checkSatDuration)
        return result
    }

}
