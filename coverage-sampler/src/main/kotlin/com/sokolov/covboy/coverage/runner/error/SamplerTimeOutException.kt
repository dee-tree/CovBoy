package com.sokolov.covboy.coverage.runner.error

import java.util.concurrent.TimeUnit

@kotlinx.serialization.Serializable
class SamplerTimeOutException(val timeout: Pair<Long, TimeUnit>) : Exception("Sampler timeout exceeded: ${timeout.first}") {
}