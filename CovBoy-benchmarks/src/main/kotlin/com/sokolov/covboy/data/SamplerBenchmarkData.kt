package com.sokolov.covboy.data

import com.sokolov.covboy.sampler.CoverageSamplerType
import kotlinx.serialization.Serializable
import org.ksmt.runner.generated.models.SolverType
import org.ksmt.solver.KSolverStatus
import java.io.BufferedWriter
import java.io.OutputStream
import kotlin.time.Duration

@Serializable
data class SamplerBenchmarkData(
    val inputFormulaPath: String,
    val duration: Duration,
    val checkSatCallsCount: Int,
    val checkSats: List<CheckSatData>,
    val predicatesCount: Int,
    val samplerType: CoverageSamplerType,
    val solverType: SolverType
) {


    val satPercentOnCheckSat: Int
        get() = (checkSats.count { it.status == KSolverStatus.SAT } * 100) / checkSatCallsCount

    val meanCheckSatDuration: Duration
        get() = checkSats.map { it.duration }.reduce(Duration::plus) / checkSatCallsCount

    val maxPredicatesCoveredOnCheckSat: Int
        get() = checkSats.maxOf { it.coveredPredicates }

    val nonEmptyCheckSatCountByCoverage: Int
        get() = checkSats.count { it.coveredPredicates > 0 }

    val meanCoveredPredicatesCountOnSat: Double
        get() = checkSats.filter { it.status == KSolverStatus.SAT }.let { sats ->
            sats.sumOf { it.coveredPredicates }.toDouble() / sats.size
        }


    private fun writeCsvTitle(writer: BufferedWriter) {
        writer.write(""" "InputFormula", """.trimStart())
        writer.write(""" "Duration (ms)", """.trimStart())
        writer.write(""" "Check-sat (count)", """.trimStart())
        writer.write(""" "Sat (%)", """.trimStart())
        writer.write(""" "Mean check-sat duration (ms)", """.trimStart())
        writer.write(""" "Max predicates covered per check-sat (count)", """.trimStart())
        writer.write(""" "Non-empty check-sat by coverage (count)", """.trimStart())
        writer.write(""" "Mean covered predicates on Sat (count)", """.trimStart())
        writer.write(""" "Predicates (count)", """.trimStart())
        writer.write(""" "Sampler (type)", """.trimStart())
        writer.write(""" "Solver (type)" """.trim())
        writer.newLine()

        writer.flush()
    }

    private fun writeCsvContent(writer: BufferedWriter) {
        writer.write("$inputFormulaPath, ")
        writer.write("${duration.inWholeMilliseconds}, ")
        writer.write("$checkSatCallsCount, ")
        writer.write("$satPercentOnCheckSat, ")
        writer.write("${meanCheckSatDuration.inWholeMilliseconds}, ")
        writer.write("$maxPredicatesCoveredOnCheckSat, ")
        writer.write("$nonEmptyCheckSatCountByCoverage, ")
        writer.write("$meanCoveredPredicatesCountOnSat, ")
        writer.write("$predicatesCount, ")
        writer.write("$samplerType, ")
        writer.write("$solverType")

        writer.newLine()

        writer.flush()
    }

    fun writeCsv(outputStream: OutputStream) {
        val writer = outputStream.bufferedWriter()

        writeCsvTitle(writer)
        writeCsvContent(writer)

        writer.flush()
    }


    companion object {
        fun writeCsv(data: List<SamplerBenchmarkData>, outputStream: OutputStream) {
            if (data.isEmpty()) return

            val writer = outputStream.bufferedWriter()
            data.first().writeCsvTitle(writer)
            data.forEach { it.writeCsvContent(writer) }
        }
    }

}

fun List<SamplerBenchmarkData>.writeCsv(outputStream: OutputStream) {
    SamplerBenchmarkData.writeCsv(this, outputStream)
}
