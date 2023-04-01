package com.sokolov.covboy.sampler.benchmarks

import com.sokolov.covboy.sampler.process.SamplerProcessParamArgs
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import org.ksmt.runner.generated.models.SolverType
import java.io.File

class BenchmarksSamplerArgs(parser: ArgParser) : SamplerProcessParamArgs(parser) {

    val solverTypes by parser.positionalList(
        "SOLVER_TYPES",
        help = "Names of the solvers to collect each formula's coverage. Possible variants: ${
            SolverType.values().joinToString("/")
        }",
        sizeRange = 1..SolverType.values().size
    ) {
        if (this.startsWith("--"))
            SolverType.valueOf(this.substring(2))
        else SolverType.valueOf(this)
    }

    val benchmarksDir: File by parser.storing(
        "-i", "--in", "--input", "--benchmarks",
        help = "Path to root dir with smt-lib v2 formulas (.smt2)"
    ) { File(this) }

    val coverageDir: File by parser.storing(
        "-o", "--out", "--output",
        help = "Path to dir to store coverage. Files structure is preserved as benchmark relative file from benchmarksDir (--benchmarks)"
    ) { File(this) }

    val rewriteCoverage: Boolean by parser.flagging(
        "-r", "--rewrite",
        help = "Rewrite collected previously coverage"
    ).default { false }

}