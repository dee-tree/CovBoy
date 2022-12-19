package com.sokolov.covboy.coverage.predicate

import org.ksmt.expr.KExpr
import org.ksmt.sort.KSort

abstract class CoveragePredicate<E : KExpr<T>, T : KSort> constructor(
    open val expr: E,
//    open val satValues: Set<E> = emptySet(),
//    open val unsatValues: Set<E> = emptySet()
) : AutoCloseable {

    protected abstract fun isCoveredOnValues(values: Set<E>): Boolean

    abstract fun getAnyUncoveredValue(): E

    abstract fun toMutable(): MutableCoveragePredicate<E, T>

    abstract operator fun minus(other: CoveragePredicate<E, T>): CoveragePredicate<E, T>

    abstract val satValues: Set<E>

    abstract val unsatValues: Set<E>

//    val coveredValues: Set<E>
//        get() = satValues + unsatValues

    val isCovered: Boolean
        get() = isCoveredOnValues(satValues + unsatValues)

    val isEmpty: Boolean
        get() = satValues.isEmpty()

    val fullCoverageOnSatAchieved: Boolean
        get() = isCoveredOnValues(satValues)


}
