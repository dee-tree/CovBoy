package com.sokolov.covboy.sampler.exceptions

open class CoverageSamplingException : Exception {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
