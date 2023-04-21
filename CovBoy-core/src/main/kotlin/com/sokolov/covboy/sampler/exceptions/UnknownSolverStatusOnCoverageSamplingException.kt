package com.sokolov.covboy.sampler.exceptions

class UnknownSolverStatusOnCoverageSamplingException : CoverageSamplingException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}