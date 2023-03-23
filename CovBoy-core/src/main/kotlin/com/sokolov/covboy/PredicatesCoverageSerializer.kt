package com.sokolov.covboy

import com.jetbrains.rd.framework.AbstractBuffer
import com.jetbrains.rd.framework.createAbstractBuffer
import com.jetbrains.rd.framework.readEnum
import com.jetbrains.rd.framework.writeEnum
import com.sokolov.covboy.coverage.PredicatesCoverageSamplingError
import org.ksmt.KAst
import org.ksmt.KContext
import org.ksmt.expr.KExpr
import org.ksmt.runner.generated.models.SolverType
import org.ksmt.runner.serializer.AstDeserializer
import org.ksmt.runner.serializer.AstSerializationCtx
import org.ksmt.runner.serializer.AstSerializer
import org.ksmt.sort.KSort
import org.ksmt.utils.uncheckedCast
import java.io.InputStream
import java.io.OutputStream

class PredicatesCoverageSerializer(private val ctx: KContext) {

    private enum class FieldMark {
        CoverageSat, CoverageUnsat, CoverageUniverse, SolverType
    }

    fun <S : KSort> PredicatesCoverage<S>.serialize(out: OutputStream) {
        val sCtx = AstSerializationCtx()
        sCtx.initCtx(ctx)

        val buffer = createAbstractBuffer()
        val serializer = AstSerializer(sCtx, buffer)

        // write serialized coverage version
        buffer.writeInt(VERSION)

        buffer.writeInt(SUCCESSFUL_COVERAGE_CODE)

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

        // write SolverType
        buffer.writeEnum(FieldMark.SolverType)
        buffer.writeEnum(solverType)

        out.write(buffer.getArray())
    }

    fun isCompleteCoverage(input: InputStream): Boolean {
        // TODO: ensure that inputStream is markable
        input.mark(0)

        val buffer = createAbstractBuffer(input.readNBytes(Int.SIZE_BYTES * 2))
        val version = buffer.readInt()

        return when (version) {
            1 -> isCompleteCoverageV1(buffer)
            else -> throw IllegalStateException("Unexpected serialized coverage version: $version, but current is $VERSION")
        }.also {
            input.reset()
        }
    }

    private fun isCompleteCoverageV1(buffer: AbstractBuffer): Boolean {
        val successCode = buffer.readInt()
        return successCode == SUCCESSFUL_COVERAGE_CODE
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

        val successCode = buffer.readInt()
        if (successCode == FAILED_COVERAGE_CODE) {
            throw IllegalStateException("No available coverage to deserialize | Error during coverage sampling")
        }

        if (successCode != SUCCESSFUL_COVERAGE_CODE) {
            throw IllegalStateException("Corrupted serialized coverage!")
        }

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

        // read SolverType
        val solverTypeField = buffer.readEnum<FieldMark>()
        if (solverTypeField != FieldMark.SolverType) {
            throw IllegalStateException("Corrupted serialized coverage!")
        }

        val solverType = buffer.readEnum<SolverType>()

        return PredicatesCoverage(
            coverageSat,
            coverageUnsat,
            coverageUniverse,
            solverType
        )
    }


    fun PredicatesCoverageSamplingError.serialize(out: OutputStream) {
        val buffer = createAbstractBuffer()

        // write serialized coverage version
        buffer.writeInt(VERSION)

        buffer.writeInt(FAILED_COVERAGE_CODE)

        buffer.writeEnum(reason)
        buffer.writeString(text)
        buffer.writeEnum(solverType)

        out.write(buffer.getArray())
    }

    fun deserializeError(input: InputStream): PredicatesCoverageSamplingError {
        val buffer = createAbstractBuffer(input.readAllBytes())

        // check serialized coverage version
        val version = buffer.readInt()

        return when (version) {
            1 -> deserializeErrorV1(buffer)
            else -> throw IllegalStateException("Unexpected serialized coverage error version: $version, but current is $VERSION")
        }
    }

    private fun deserializeErrorV1(buffer: AbstractBuffer): PredicatesCoverageSamplingError {
        val successCode = buffer.readInt()
        if (successCode == SUCCESSFUL_COVERAGE_CODE) {
            throw IllegalStateException("No coverage sampling error! Coverage completed successfully")
        }

        if (successCode != FAILED_COVERAGE_CODE) {
            throw IllegalStateException("Corrupted serialized coverage error!")
        }

        val reason = buffer.readEnum<PredicatesCoverageSamplingError.Reasons>()
        val msg = buffer.readString()
        val solverType = buffer.readEnum<SolverType>()

        return PredicatesCoverageSamplingError(reason, msg, solverType)
    }


    companion object {
        private val VERSION = 1

        private val SUCCESSFUL_COVERAGE_CODE = 1 // on coverage successfully collected - serialize it
        private val FAILED_COVERAGE_CODE = -1 // on any errors during sampling
    }

}