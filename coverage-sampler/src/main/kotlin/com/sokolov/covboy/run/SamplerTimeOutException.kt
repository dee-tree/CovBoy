package com.sokolov.covboy.run

import java.util.concurrent.TimeUnit

@kotlinx.serialization.Serializable
class SamplerTimeOutException(val timeout: Pair<Long, TimeUnit>) : Exception("Sampler timeout exceeded: ${timeout.first} ") {
}