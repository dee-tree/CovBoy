package com.sokolov.covboy.coverage.predicate

import kotlinx.serialization.Serializable
import org.ksmt.KContext

@Serializable
data class SerializableCoveragePredicate(
    val expr: String,
    val satValues: Set<String>
)

fun CoveragePredicate<*, *>.toSerializable(ctx: KContext): SerializableCoveragePredicate {
    val expr = buildString { expr.print(this) }
    val satValues = satValues.map { buildString { it.print(this) } }.toSet()

    return SerializableCoveragePredicate(expr, satValues)
}
