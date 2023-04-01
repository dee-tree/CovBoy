package com.sokolov.covboy.sampler.main

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
}
