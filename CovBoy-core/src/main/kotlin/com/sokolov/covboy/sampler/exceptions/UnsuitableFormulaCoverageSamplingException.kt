package com.sokolov.covboy.sampler.exceptions

class UnsuitableFormulaCoverageSamplingException : CoverageSamplingException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
