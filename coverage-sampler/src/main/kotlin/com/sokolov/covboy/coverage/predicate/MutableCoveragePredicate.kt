package com.sokolov.covboy.coverage.predicate

import org.ksmt.expr.KExpr
import org.ksmt.sort.KSort

abstract class MutableCoveragePredicate<E : KExpr<T>, T : KSort> constructor(
    expr: E,
    satValues: Set<E> = emptySet(),
    unsatValues: Set<E> = emptySet()
) : CoveragePredicate<E, T>(expr) {

    abstract fun toImmutable(): CoveragePredicate<E, T>

    private val _satValues = satValues.toMutableSet()

    private val _unsatValues = unsatValues.toMutableSet()

    final override val satValues: Set<E>
        get() = _satValues.toSet()

    override val unsatValues: Set<E>
        get() = _unsatValues.toSet()


    /**
     * @return `true`, if [value] previously wasn't covered, and `false` if coverage not changed
     */
    fun cover(value: E): Boolean {
        return _satValues.add(value)
    }

    /**
     * @return `true`, if [value] previously wasn't added
     */
    fun fixUnsatValue(value: E): Boolean {
        return _unsatValues.add(value)
    }

    fun fixNoMoreSatValues() {
        while (!isCovered) {
            fixUnsatValue(getAnyUncoveredValue())
        }
    }

    override fun close() {
        _satValues.clear()
        _unsatValues.clear()
    }
}
