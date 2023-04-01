package com.sokolov.covboy.sampler.process

import com.sokolov.covboy.sampler.main.SamplerParamArgs
import com.sokolov.covboy.sampler.params.CoverageSamplerParams
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

open class SamplerProcessParamArgs(parser: ArgParser) : SamplerParamArgs(parser) {
    val samplerTimeoutMillis by parser.storing(
        "--timeout", "--samplerTimeoutMillis",
        help = "Timeout, in millis, on full coverage sampling; 0 to disable timeout"
    ) { this.toLong() }
        .default { SamplerProcessRunner.DEFAULT_SAMPLER_TIMEOUT.inWholeMilliseconds }
        .addValidator { if (value < 0) throw IllegalArgumentException("Sampler timeout millis must be non-negative!") }

    override val params: CoverageSamplerParams
        get() = super.params + CoverageSamplerParams.build {
            putSamplerTimeoutMillis(samplerTimeoutMillis)
        }
}
