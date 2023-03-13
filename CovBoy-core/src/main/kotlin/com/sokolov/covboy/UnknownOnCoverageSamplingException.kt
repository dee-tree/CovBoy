package com.sokolov.covboy

class UnknownOnCoverageSamplingException : IllegalStateException {

    constructor(msg: String): super(msg)

    constructor(): super()
}