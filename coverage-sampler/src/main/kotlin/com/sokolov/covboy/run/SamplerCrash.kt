package com.sokolov.covboy.run

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.File

@kotlinx.serialization.Serializable
data class SamplerCrash (val reason: Reasons, val text: String = "") {

    enum class Reasons {
        TIMEOUT, EXCEPTION
    }

    fun writeToFile(file: File) {
        file.writeText(Json.encodeToString(this))
    }

    fun readFromFile(file: File): SamplerCrash {
        return Json.decodeFromStream(file.inputStream())
    }
}