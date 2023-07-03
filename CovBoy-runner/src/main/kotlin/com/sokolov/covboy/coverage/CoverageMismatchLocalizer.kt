package com.sokolov.covboy.coverage

import com.sokolov.covboy.sampler.impl.MultiplePredicatesPropagatingCoverageSampler
import org.ksmt.KContext
import org.ksmt.runner.generated.createInstance
import org.ksmt.runner.generated.models.SolverType
import org.ksmt.sort.KBoolSort
import java.io.File

class CoverageMismatchLocalizer {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            val covFile1 = File(args[0])
            val covFile2 = File(args[1])

            val solverType1 = SolverType.valueOf(args[2])
            val solverType2 = SolverType.valueOf(args[3])

            val ctx = KContext(simplificationMode = KContext.SimplificationMode.NO_SIMPLIFY)

//            val solver1 = solverType1.createInstance(ctx)
//            val solver2 = solverType2.createInstance(ctx)

            val coverage1 = PredicatesCoverage.deserialize<KBoolSort>(ctx, covFile1.inputStream().buffered())
            val coverage2 = PredicatesCoverage.deserialize<KBoolSort>(ctx, covFile2.inputStream().buffered())

            // check cov1 in sat, but cov2 unsat
            val cov1FalseSat = coverage1.coverageSat.filter { (predicate, values) ->
                values.any { it in (coverage2.coverageUnsat[predicate] ?: emptySet()) }
            }

            // check cov1 in unsat, but cov2 sat
            val cov1FalseUnsat = coverage1.coverageUnsat.filter { (predicate, values) ->
                values.any { it in (coverage2.coverageSat[predicate] ?: emptySet()) }
            }


            println(".")
        }
    }
}