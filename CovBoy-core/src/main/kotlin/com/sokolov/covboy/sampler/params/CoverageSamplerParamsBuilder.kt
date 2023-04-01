package com.sokolov.covboy.sampler.params

class CoverageSamplerParamsBuilder {

    private val paramsStorage = hashMapOf<String, Any>()

    fun putParam(key: String, value: Any) {
        paramsStorage[key] = value
    }

    fun build(): CoverageSamplerParams = CoverageSamplerParams(paramsStorage)
}
