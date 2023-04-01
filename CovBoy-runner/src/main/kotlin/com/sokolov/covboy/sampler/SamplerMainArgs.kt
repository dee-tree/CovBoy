package com.sokolov.covboy.sampler

import com.xenomachina.argparser.ArgParser
import org.ksmt.runner.generated.models.SolverType
import java.io.File

class SamplerMainArgs(parser: ArgParser) : SamplerParamArgs(parser) {
    val solverType by parser.mapping(
        "--${SolverType.Z3.name}" to SolverType.Z3,
        "--${SolverType.Cvc5.name}" to SolverType.Cvc5,
        "--${SolverType.Bitwuzla.name}" to SolverType.Bitwuzla,
        "--${SolverType.Yices.name}" to SolverType.Yices,
        help = "Name of the solver. Possible variants: ${SolverType.values().joinToString(" / ")}"
    )

    val smtLibFormulaFile: File by parser.storing(
        "-i", "--in", "--input",
        help = "Path to smt-lib v2 formula (.smt2)"
    ) { File(this) }

    val coverageFile: File by parser.storing(
        "-o", "--out", "--output",
        help = "Path to output of coverage (.cov)"
    ) { File(this) }

    /*    val coverageSamplerType by parser.mapping(
            "--${CoverageSamplerType.PredicatesPropagatingSampler}" to CoverageSamplerType.PredicatesPropagatingSampler,
            "--baseline" to CoverageSamplerType.PredicatesPropagatingSampler,
            "--${CoverageSamplerType.GroupingModelsSampler}" to CoverageSamplerType.GroupingModelsSampler,
            help = "Name of the coverage sampler type. Possible variants: ${
                CoverageSamplerType.values().joinToString(" / ")
            }"
        ).default { CoverageSamplerType.PredicatesPropagatingSampler }

        val solverTimeoutMillis by parser.storing(
            "--stm", "--solverTimeoutMillis",
            help = "Timeout, in millis, on each solver check-sat() operation; 0 to disable timeout"
        ) { this.toLong() }
            .addValidator { if (value < 0) throw IllegalArgumentException("Solver timeout millis must be non-negative!") }
            .default { CoverageSampler.DEFAULT_SOLVER_TIMEOUT.inWholeMilliseconds }

        val completeModels by parser.storing(
            "--completeModels", "--cm",
            help = "Should force solver to provide complete models (with assigning value for each predicate) - true; Or to provide incomplete models (to cover non-model-core predicates lazily) - false"
        ) { this.toBooleanStrict() }.default { CoverageSampler.DEFAULT_COMPLETE_MODELS }

        val modelsGroupSize by parser.storing(
            "--mgs", "--modelsGroupSize",
            help = "Size of bounded models in group. Valuable in ${CoverageSamplerType.GroupingModelsSampler}"
        ) { this.toInt() }
            .addValidator { if (value < 1) throw IllegalArgumentException("Models groupd size must be positive value!") }
            .default { GroupingModelsCoverageSampler.DEFAULT_MODELS_GROUP_SIZE }*/
}
