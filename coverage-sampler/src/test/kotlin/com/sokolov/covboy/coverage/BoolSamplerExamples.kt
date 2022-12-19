package com.sokolov.covboy.coverage

import com.sokolov.covboy.coverage.predicate.bool.CoverageBoolPredicate
import com.sokolov.covboy.utils.KBoolExpr
import org.ksmt.KContext
import org.ksmt.expr.KApp
import org.ksmt.expr.KBitVec8Value
import org.ksmt.expr.KExpr
import org.ksmt.sort.KBoolSort
import org.ksmt.sort.KBvSort


val baseSamplerExamples = listOf<(ctx: KContext) -> DynamicSamplerTest<KBoolExpr, KBoolSort>>(
    /*
    a: BoolConst
    assert:
        a
    expected:
        a = true
    */
    { ctx: KContext ->
        with(ctx) {
            val a = mkConst("a", boolSort)

            val aCoverage = CoverageBoolPredicate(a, this).apply {
                cover(mkTrue())
            }

            DynamicSamplerTest("a: BoolConst", listOf(a), FormulaCoverage(setOf(aCoverage)))
        }

    },

    /*
    a: BoolConst
    assert:
        !a
    expected:
        a = false
    */
    { ctx: KContext ->
        with(ctx) {
            val a = mkConst("a", boolSort)

            val aCoverage = CoverageBoolPredicate(a, this).apply {
                cover(mkFalse())
            }

            DynamicSamplerTest("a: BoolConst", listOf(!a), FormulaCoverage(setOf(aCoverage)))
        }

    },

    /*
    a: BoolConst, b: BoolConst
    assert:
        a || b
    expected:
        a = false|true
        b = false|true
    */
    { ctx: KContext ->
        with(ctx) {
            val a = mkConst("a", boolSort)
            val b = mkConst("b", boolSort)

            val aCoverage = CoverageBoolPredicate(a, this).apply {
                cover(mkTrue())
                cover(mkFalse())
            }
            val bCoverage = CoverageBoolPredicate(b, this).apply {
                cover(mkTrue())
                cover(mkFalse())
            }

            DynamicSamplerTest("a or b", listOf(a or b), FormulaCoverage(setOf(aCoverage, bCoverage)))
        }
    },

    /*
    a: BoolConst, b: BoolConst
    assert:
        a && b
    expected:
        a = true
        b = true
    */
    { ctx: KContext ->
        with(ctx) {
            val a = mkConst("a", boolSort)
            val b = mkConst("b", boolSort)

            val aCoverage = CoverageBoolPredicate(a, this).apply {
                cover(mkTrue())
            }
            val bCoverage = CoverageBoolPredicate(b, this).apply {
                cover(mkTrue())
            }

            DynamicSamplerTest("a and b", listOf(a and b), FormulaCoverage(setOf(aCoverage, bCoverage)))
        }
    },

    /*
    a: BoolConst, b: BoolConst
    assert:
        a => b
    expected:
        a = false|true
        b = false|true
    */
    { ctx: KContext ->
        with(ctx) {
            val a = mkConst("a", boolSort)
            val b = mkConst("b", boolSort)

            val aCoverage = CoverageBoolPredicate(a, this).apply {
                cover(mkTrue())
                cover(mkFalse())
            }
            val bCoverage = CoverageBoolPredicate(b, this).apply {
                cover(mkTrue())
                cover(mkFalse())
            }

            DynamicSamplerTest("a => b", listOf(a implies b), FormulaCoverage(setOf(aCoverage, bCoverage)))
        }
    },

    /*
    a: BoolConst, b: BoolConst
    assert:
        a xor b
    expected:
        a = false|true
        b = false|true
    */
    { ctx: KContext ->
        with(ctx) {
            val a = mkConst("a", boolSort)
            val b = mkConst("b", boolSort)

            val aCoverage = CoverageBoolPredicate(a, this).apply {
                cover(mkTrue())
                cover(mkFalse())
            }
            val bCoverage = CoverageBoolPredicate(b, this).apply {
                cover(mkTrue())
                cover(mkFalse())
            }

            DynamicSamplerTest("a xor b", listOf(a xor b), FormulaCoverage(setOf(aCoverage, bCoverage)))
        }
    },

    /*
    a: BoolConst, b: BoolConst, c: BoolConst
    assert:
        true => (a and b and !c)
    expected:
        a = true
        b = true
        c = false
    */
    { ctx: KContext ->
        with(ctx) {
            val a = mkConst("a", boolSort)
            val b = mkConst("b", boolSort)
            val c = mkConst("c", boolSort)

            val aCoverage = CoverageBoolPredicate(a, this).apply {
                cover(mkTrue())
            }
            val bCoverage = CoverageBoolPredicate(b, this).apply {
                cover(mkTrue())
            }
            val cCoverage = CoverageBoolPredicate(c, this).apply {
                cover(mkFalse())
            }

            DynamicSamplerTest(
                "true => (a and b and !c)",
                listOf(mkTrue() implies (a and b and !c)),
                FormulaCoverage(setOf(aCoverage, bCoverage, cCoverage))
            )
        }
    },

    )

val QFBVSamplerExamples = listOf<(ctx: KContext) -> DynamicSamplerTest<KBoolExpr, KBoolSort>>(
    /*
    a: Const(BV_1)
    assert:
        a != 1
    expected:
        (a = 1) = false
    */
    { ctx: KContext ->
        with(ctx) {
            val a = mkConst("a", bv1Sort)

            val bvEqExpr = a eq mkBv(true)

            val aCoverage = CoverageBoolPredicate(bvEqExpr, this).apply {
                cover(mkFalse())
            }

            DynamicSamplerTest("a != 1", listOf(!bvEqExpr), FormulaCoverage(setOf(aCoverage)))
        }

    },

    /*
    a: Const(BV_8)
    assert:
        a <u 01110011 || a >u 11111111
    expected:
        (a <u 01110011) = true
    */
    { ctx: KContext ->
        with(ctx) {
            val a = mkConst("a", bv8Sort)

            val bvULTExpr = mkBvUnsignedLessExpr(a, mkBv(0b01110011, bv8Sort))
            val bvUGTExpr = mkBvUnsignedGreaterExpr(a, mkBv(0b11111111, bv8Sort))

            val bvULTExprCoverage = CoverageBoolPredicate(bvULTExpr, this).apply {
                cover(mkTrue())
            }
            val bvUGTExprCoverage = CoverageBoolPredicate(bvUGTExpr, this).apply {
                cover(mkFalse())
            }

            DynamicSamplerTest("a <u 01110011 || a >u 11111111", listOf(bvULTExpr or bvUGTExpr), FormulaCoverage(setOf(bvUGTExprCoverage, bvULTExprCoverage)))
        }
    },
)


val boolSamplerExamples = baseSamplerExamples + QFBVSamplerExamples
