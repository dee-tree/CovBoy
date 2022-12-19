package com.sokolov.covboy.coverage.predicate.bool

import com.sokolov.covboy.coverage.predicate.CoveragePredicate
import com.sokolov.covboy.coverage.predicate.MutableCoveragePredicate
import com.sokolov.covboy.utils.KBoolExpr
import org.ksmt.KContext
import org.ksmt.expr.KFalse
import org.ksmt.expr.KTrue
import org.ksmt.sort.KBoolSort
import java.util.*

class CoverageBoolPredicate(
    override val expr: KBoolExpr,
    private val ctx: KContext,
    satValues: Set<KBoolExpr> = emptySet(),
    unsatValues: Set<KBoolExpr> = emptySet()
) : MutableCoveragePredicate<KBoolExpr, KBoolSort>(expr, satValues, unsatValues) {
    override fun isCoveredOnValues(values: Set<KBoolExpr>) = values.hasTrue() && values.hasFalse()

    override fun getAnyUncoveredValue(): KBoolExpr = when {
        !(satValues + unsatValues).hasTrue() -> ctx.mkTrue()
        !(satValues + unsatValues).hasFalse() -> ctx.mkFalse()
        else -> throw IllegalStateException("No uncovered values: all values covered!")
    }

    override fun toMutable(): MutableCoveragePredicate<KBoolExpr, KBoolSort> = copy()

    override fun toImmutable(): CoveragePredicate<KBoolExpr, KBoolSort> = copy()

    override fun minus(other: CoveragePredicate<KBoolExpr, KBoolSort>): CoveragePredicate<KBoolExpr, KBoolSort> {
        check(expr == other.expr)

        return copy(satValues = satValues - other.satValues, unsatValues = unsatValues - other.unsatValues)
    }

    fun copy(
        satValues: Set<KBoolExpr> = this.satValues,
        unsatValues: Set<KBoolExpr> = this.unsatValues
    ): CoverageBoolPredicate = CoverageBoolPredicate(expr, ctx).also { pr ->
        satValues.forEach {
            pr.cover(it)
        }
        unsatValues.forEach {
            pr.fixUnsatValue(it)
        }
    }

    fun Collection<KBoolExpr>.hasTrue() = any { it is KTrue }
    fun Collection<KBoolExpr>.hasFalse() = any { it is KFalse }


    override fun equals(other: Any?): Boolean = other is CoverageBoolPredicate
            && expr == other.expr
            && satValues == other.satValues

    override fun hashCode(): Int {
        return Objects.hash(satValues, unsatValues)
    }

    override fun toString(): String = "CoverageBoolPredicate($expr, sat=$satValues, unsat=$unsatValues)"
}