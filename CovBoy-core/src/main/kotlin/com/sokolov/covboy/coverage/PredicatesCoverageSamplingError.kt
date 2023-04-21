package com.sokolov.covboy.coverage

import org.ksmt.KContext
import org.ksmt.runner.generated.models.SolverType
import java.io.InputStream
import java.io.OutputStream

data class PredicatesCoverageSamplingError(
    val reason: Reasons,
    val text: String,
    val solverType: SolverType
) {
    enum class Reasons {
        UnknownDuringSampling, ProcessCrashed, TimeoutExceeded, Other, InitiallyUnsuitableFormulaSatisfiability
    }

    fun serialize(ctx: KContext, out: OutputStream) = with(PredicatesCoverageSerializer(ctx)) {
        serialize(out)
    }

    companion object {
        fun deserialize(ctx: KContext, input: InputStream): PredicatesCoverageSamplingError {
            return PredicatesCoverageSerializer(ctx).deserializeError(input)
        }
    }
}
