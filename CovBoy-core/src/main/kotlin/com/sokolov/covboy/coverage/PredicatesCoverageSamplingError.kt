package com.sokolov.covboy.coverage

data class PredicatesCoverageSamplingError(val reason: Reasons, val text: String) {
    enum class Reasons {
        UnknownDuringSampling, ProcessCrashed, TimeoutExceeded, Other
    }
}
