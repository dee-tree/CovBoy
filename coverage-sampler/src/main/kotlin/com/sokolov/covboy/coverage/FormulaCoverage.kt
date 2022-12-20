package com.sokolov.covboy.coverage

import com.sokolov.covboy.coverage.predicate.CoveragePredicate
import com.sokolov.covboy.coverage.predicate.SerializableCoveragePredicate
import com.sokolov.covboy.coverage.predicate.toSerializable
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import org.ksmt.KContext
import org.ksmt.expr.KExpr
import org.ksmt.sort.KSort
import java.io.File

data class FormulaCoverage<E : KExpr<T>, T : KSort>(
    val predicates: Set<CoveragePredicate<E, T>>
) : Comparable<FormulaCoverage<E, T>> {

    fun isEmpty(): Boolean = predicates.all { it.isEmpty }

    override fun compareTo(other: FormulaCoverage<E, T>): Int = compareValuesBy(this, other,
        { it.coveredValuesCount },
        { it.coveredPredicatesCount }
    )

    fun predicateByExpr(expr: E): CoveragePredicate<E, T> = predicates.first { it.expr == expr }

    val coveredPredicatesCount = predicates.count { it.isCovered }

    val coveredValuesCount = predicates.sumOf { it.satValues.size }

    operator fun minus(other: FormulaCoverage<E, T>): FormulaCoverage<E, T> = FormulaCoverage(
        predicates.map { it - other.predicateByExpr(it.expr) }.filter { !it.isEmpty }.toSet()
    )

    fun dumpToString(ctx: KContext): String = buildString {
        predicates.forEach {
            appendLine(it.dumpToString(ctx))
        }
    }

    fun dumpToFile(ctx: KContext, file: File) {
        file.parentFile.mkdirs()
        Json.encodeToStream(toSerializable(ctx), file.outputStream())
    }
}

@Serializable
data class SerializableFormulaCoverage(
    val predicate: Set<SerializableCoveragePredicate>
)

fun FormulaCoverage<*, *>.toSerializable(ctx: KContext): SerializableFormulaCoverage = SerializableFormulaCoverage(
    predicates.map { it.toSerializable(ctx) }.toSet()
)

fun <E : KExpr<T>, T : KSort> Collection<CoveragePredicate<E, T>>.toCoverage(): FormulaCoverage<E, T> =
    FormulaCoverage(this.toSet())