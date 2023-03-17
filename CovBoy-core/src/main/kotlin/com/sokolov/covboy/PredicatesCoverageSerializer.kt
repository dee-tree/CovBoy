package com.sokolov.covboy

import com.jetbrains.rd.framework.AbstractBuffer
import com.jetbrains.rd.framework.createAbstractBuffer
import com.jetbrains.rd.framework.readEnum
import com.jetbrains.rd.framework.writeEnum
import org.ksmt.KAst
import org.ksmt.KContext
import org.ksmt.expr.KExpr
import org.ksmt.runner.serializer.AstDeserializer
import org.ksmt.runner.serializer.AstSerializationCtx
import org.ksmt.runner.serializer.AstSerializer
import org.ksmt.sort.KSort
import org.ksmt.utils.uncheckedCast
import java.io.InputStream
import java.io.OutputStream

class PredicatesCoverageSerializer(private val ctx: KContext) {

    private enum class FieldMark {
        CoverageSat, CoverageUnsat, CoverageUniverse
    }

    fun <S : KSort> PredicatesCoverage<S>.serialize(out: OutputStream) {
        val sCtx = AstSerializationCtx()
        sCtx.initCtx(ctx)

        val buffer = createAbstractBuffer()
        val serializer = AstSerializer(sCtx, buffer)

        // write serialized coverage version
        buffer.writeInt(VERSION)

        // write CoverageSat
        buffer.writeEnum(FieldMark.CoverageSat)
        buffer.writeInt(coverageSat.size)

        coverageSat.forEach { (key, values) ->
            serializer.serializeAst(key)
            buffer.writeInt(values.size)
            values.forEach(serializer::serializeAst)
        }

        // write CoverageUnsat
        buffer.writeEnum(FieldMark.CoverageUnsat)
        buffer.writeInt(coverageUnsat.size)

        coverageUnsat.forEach { (key, values) ->
            serializer.serializeAst(key)
            buffer.writeInt(values.size)
            values.forEach(serializer::serializeAst)
        }

        // write CoverageUniverse
        buffer.writeEnum(FieldMark.CoverageUniverse)
        buffer.writeInt(coverageUniverse.size)
        coverageUniverse.forEach(serializer::serializeAst)

        out.write(buffer.getArray())
    }

    fun <S : KSort> deserialize(input: InputStream): PredicatesCoverage<S> {
        val sCtx = AstSerializationCtx()
        sCtx.initCtx(ctx)

        val buffer = createAbstractBuffer(input.readAllBytes())
        val deserializer = AstDeserializer(sCtx, buffer)

        // check serialized coverage version
        val version = buffer.readInt()

        return when (version) {
            1 -> deserializeV1(deserializer, buffer)
            else -> throw IllegalStateException("Unexpected serialized coverage version: $version, but current is $VERSION")
        }

    }

    private fun <S : KSort> deserializeV1(
        deserializer: AstDeserializer,
        buffer: AbstractBuffer
    ): PredicatesCoverage<S> {
        // read CoverageSat
        val coverageSatField = buffer.readEnum<FieldMark>()
        if (coverageSatField != FieldMark.CoverageSat) {
            throw IllegalStateException("Corrupted serialized coverage!")
        }

        val coverageSatSize = buffer.readInt()
        val coverageSat = HashMap<KExpr<S>, Set<KExpr<S>>>(coverageSatSize)

        repeat(coverageSatSize) {
            val key = deserializer.deserializeAst()
            val valuesSize = buffer.readInt()

            val values = HashSet<KExpr<S>>(valuesSize)
            repeat(valuesSize) { values += deserializer.deserializeAst().uncheckedCast<KAst, KExpr<S>>() }

            coverageSat[key.uncheckedCast()] = values
        }

        // read CoverageUnsat
        val coverageUnsatField = buffer.readEnum<FieldMark>()
        if (coverageUnsatField != FieldMark.CoverageUnsat) {
            throw IllegalStateException("Corrupted serialized coverage!")
        }

        val coverageUnsatSize = buffer.readInt()
        val coverageUnsat = HashMap<KExpr<S>, Set<KExpr<S>>>(coverageUnsatSize)

        repeat(coverageUnsatSize) {
            val key = deserializer.deserializeAst()
            val valuesSize = buffer.readInt()

            val values = HashSet<KExpr<S>>(valuesSize)
            repeat(valuesSize) { values += deserializer.deserializeAst().uncheckedCast<KAst, KExpr<S>>() }

            coverageUnsat[key.uncheckedCast()] = values
        }

        // read CoverageUniverse
        val coverageUniverseField = buffer.readEnum<FieldMark>()
        if (coverageUniverseField != FieldMark.CoverageUniverse) {
            throw IllegalStateException("Corrupted serialized coverage!")
        }

        val coverageUniverseSize = buffer.readInt()
        val coverageUniverse = HashSet<KExpr<S>>(coverageUniverseSize)

        repeat(coverageUniverseSize) {
            coverageUniverse += deserializer.deserializeAst().uncheckedCast<KAst, KExpr<S>>()
        }

        return PredicatesCoverage(
            coverageSat,
            coverageUnsat,
            coverageUniverse
        )
    }


    companion object {
        private val VERSION = 1
    }

}