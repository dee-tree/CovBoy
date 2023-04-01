package com.sokolov.covboy.sampler.params

open class CoverageSamplerParams(private val params: Map<String, Any>) {

    fun hasParam(key: String): Boolean = key in params
    fun hasStringParam(key: String): Boolean = params[key] is String
    fun hasIntParam(key: String): Boolean = params[key] is Int
    fun hasLongParam(key: String): Boolean = params[key] is Long
    fun hasBoolParam(key: String): Boolean = params[key] is Boolean


    fun getString(key: String): String = params[key] as String
    fun getInt(key: String): Int = params[key] as Int
    fun getLong(key: String): Long = params[key] as Long
    fun getBool(key: String): Boolean = params[key] as Boolean

    companion object {
        fun build(builderAction: CoverageSamplerParamsBuilder.() -> Unit): CoverageSamplerParams =
            CoverageSamplerParamsBuilder().apply(builderAction).build()
    }

    operator fun plus(other: CoverageSamplerParams): CoverageSamplerParams {
        return CoverageSamplerParams(params + other.params)
    }

    object Empty : CoverageSamplerParams(emptyMap())
}
