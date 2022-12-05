package com.sokolov.covboy.solvers.provers

interface ChecksCounter {
    val sat: Int
    val unsat: Int
    val unknown: Int
    val total: Int
}

internal class MutableChecksCounter(
    _sat: Int = 0,
    _unsat: Int = 0,
    _unknown: Int = 0
) : ChecksCounter {
    override var sat: Int = _sat
        private set

    override var unsat: Int = _unsat
        private set

    override var unknown: Int = _unknown
        private set

    fun update(status: Status): Unit = when (status) {
        Status.SAT -> sat += 1
        Status.UNSAT -> unsat += 1
        Status.UNKNOWN -> unknown += 1
    }

    override val total: Int
        get() = sat + unsat + unknown

    override fun toString(): String = "ChecksCounter(sat = $sat, unsat = $unsat, unknown = $unknown, total = $total)"
}

