package com.sokolov.covboy.data

import kotlinx.serialization.Serializable
import org.ksmt.solver.KSolverStatus
import kotlin.time.Duration

@Serializable
data class CheckSatData(
    val duration: Duration,
    val status: KSolverStatus,
    // covered by this check-sat call
    val coveredPredicates: Int
) {

}
