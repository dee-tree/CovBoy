package com.sokolov.covboy.run

import com.sokolov.covboy.coverage.CoverageResult
import com.sokolov.covboy.coverage.provider.CoverageSamplerProvider
import com.sokolov.covboy.solvers.provers.provider.makeProver
import org.sosy_lab.java_smt.SolverContextFactory.Solvers
import org.sosy_lab.java_smt.api.BooleanFormula
import java.io.File

class CoverageDiffDumper(
    val inputFile: File,
    val baseSolver: Solvers,
    val otherSolver: Solvers,
    provideSample: CoverageSamplerProvider
) {
    private val baseProver = makeProver(baseSolver).apply { addConstraintsFromFile(inputFile) }
    private val otherProver = makeProver(false, otherSolver).apply { addConstraintsFromFile(inputFile) }

    val baseSampler = provideSample(baseProver)
    val otherSampler = provideSample(otherProver)

    fun dumpToFile(filePrefix: String, dir: File, rewrite: Boolean) {
        val firstFile = File(dir, "$filePrefix-$baseSolver-$otherSolver.smt2")
        val secondFile = File(dir, "$filePrefix-$otherSolver-$baseSolver.smt2")

        if (!rewrite && secondFile.exists() && secondFile.length() > 0 && firstFile.exists() && firstFile.length() > 0) {
            return
        }

        val originalFormula = baseProver.fm.booleanFormulaManager.and(baseProver.formulas)
        val baseCoverage: CoverageResult = baseSampler.computeCoverage()
        val otherCoverage: CoverageResult = otherSampler.computeCoverage()

        firstFile.writeText(dumpCase(originalFormula, baseCoverage, otherCoverage))
        secondFile.writeText(dumpCase(originalFormula, otherCoverage, baseCoverage))

        close()
    }

    private fun dumpCase(originalFormula: BooleanFormula, firstCov: CoverageResult, secondCov: CoverageResult): String {
        return buildString {
            // asserted atoms

            val assertedFormula = baseProver.fm.booleanFormulaManager.and((firstCov - secondCov).map { it.assignmentAsFormula })

            baseProver.fm.dumpFormula(baseProver.fm.booleanFormulaManager.and(originalFormula, assertedFormula)).appendTo(this)

            appendLine()
            appendLine("(check-sat)")
        }
    }


    fun close() {
        baseProver.close()
        baseProver.context.close()

        otherProver.close()
        otherProver.context.close()
    }
}

