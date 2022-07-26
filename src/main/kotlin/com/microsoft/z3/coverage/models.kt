package com.microsoft.z3

import java.util.UUID


fun Solver.allModels(limit: Int = 10): Set<Model> = buildSet {

    var i = 0
    whileSat { model ->
        if (i > limit) return@whileSat
        add(model)
        val modelExprs = model.constDecls.map { decl ->
            (context.mkConst(decl) eq model.getConstInterp(decl))
        }

        i++

        add(!context.and(*modelExprs.toTypedArray()))
    }
}

fun Solver.walk(action: (Expr) -> Boolean) {
    assertions.forEach { it.walk(action) }
}

fun <T> Solver.deepestBoolExprs(action: (BoolExpr) -> T): List<T> = buildList {
    walk { expr ->
        if (expr.isApp) {
            if (expr.numArgs > 0 && expr.args.any { it.isBool }) true else {
                (expr as? BoolExpr)?.also { this@buildList.add(action(it)) }
                false
            }
        } else {
            if (expr.isQuantifier) add(action(expr as Quantifier))
            false
        }
    }
}

fun Solver.deepestBoolExprs(): Set<BoolExpr> = deepestBoolExprs { it }.toSet()


fun Solver.enumerateModels(): Sequence<Model> = sequence {
    while (check() == Status.SATISFIABLE) {

        push(); check()
        println("Found new constraints...")
        val currentConstraints = deepestBoolExprs().map { it to model.eval(it, true) }

        yield(model)

        assertAndTrack(context.nand(*(currentConstraints.filter { it.second == context.mkTrue() }.map { it.first } + currentConstraints.filter { it.second == context.mkFalse() }.map { !it.first }).toTypedArray()), context.mkBoolConst(UUID.randomUUID().toString()))
        if(check() == Status.UNSATISFIABLE) {
            pop()
            add(context.or(*(currentConstraints.filter { it.second == context.mkTrue() }.map { !it.first } + currentConstraints.filter { it.second == context.mkFalse() }.map { it.first }).toTypedArray()))
        }

    }
}

fun Expr.walk(action: (Expr) -> Boolean) {
    if (!action(this))
        return
    when {
        isApp -> {
            args.forEach {
                it.walk(action)
            }
        }

        isQuantifier -> {
            (this as Quantifier).body.walk(action)
        }

        isVar -> {
            println("var: $this")
        }
    }
}

val Model.constDeclsAsExpressions: List<Expr>
    get() = buildList {
        constDecls.forEach { decl ->
            add(context.mkConst(decl) eq getConstInterp(decl))
        }
    }

