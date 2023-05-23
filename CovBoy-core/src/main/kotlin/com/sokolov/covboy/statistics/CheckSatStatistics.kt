package com.sokolov.covboy.statistics

import org.ksmt.solver.KSolverStatus
import kotlin.time.Duration

data class CheckSatStatistics(
    val duration: Duration,
    val status: KSolverStatus,
    // covered by this check-sat call
    val coveredPredicates: Int
) {

}
