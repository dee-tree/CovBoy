package com.sokolov.covboy.coverage.runner.error

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File

@kotlinx.serialization.Serializable
data class SamplerCrashInfo(val reason: Reasons, val text: String = "") {

    enum class Reasons {
        TIMEOUT, EXCEPTION
    }

    fun writeToFile(file: File) {
        file.parentFile.mkdirs()
        file.writeText(Json.encodeToString(this))
    }

    fun readFromFile(file: File): SamplerCrashInfo {
        return Json.decodeFromStream(file.inputStream())
    }
}