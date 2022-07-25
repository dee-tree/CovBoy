package com.microsoft.z3


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

