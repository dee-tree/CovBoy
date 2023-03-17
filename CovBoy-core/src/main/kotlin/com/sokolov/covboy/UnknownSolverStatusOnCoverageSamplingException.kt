package com.sokolov.covboy

class UnknownSolverStatusOnCoverageSamplingException : IllegalStateException {

    constructor(msg: String): super(msg)

    constructor(): super()
}