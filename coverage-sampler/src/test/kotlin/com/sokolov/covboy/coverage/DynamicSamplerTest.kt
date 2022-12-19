package com.sokolov.covboy.coverage

import com.sokolov.covboy.coverage.sampler.CoverageSampler
import com.sokolov.covboy.utils.KBoolExpr
import org.ksmt.expr.KExpr
import org.ksmt.sort.KSort

data class DynamicSamplerTest<E : KExpr<T>, T : KSort>(
    val testName: String,
    val assertions: List<KBoolExpr>,
    val expectedCoverage: FormulaCoverage<E, T>
)

data class DynamicSamplerTestInput(val testCase: DynamicSamplerTest<*, *>, val sampler: CoverageSampler<*>)