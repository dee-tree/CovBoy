package com.microsoft.z3


fun Solver.allModels(limit: Int = 10): Set<Model> = buildSet {

    var i = 0
    whileSat { model ->
        if (i > limit) return@whileSat context.mkFalse()
        add(model)
        val modelExprs = model.constDecls.map { decl ->
            (context.mkConst(decl) eq model.getConstInterp(decl))
        }

        i++

        !context.and(*modelExprs.toTypedArray())
    }
}

fun Solver.walk(action: (Expr) -> Boolean) {
    assertions.forEach { it.walk(action) }
}

fun Solver.deepestBoolExprs(action: (BoolExpr) -> Unit) {
    walk { expr ->
        if (expr.isApp) {
            if (expr.numArgs > 0) true else {
                (expr as? BoolExpr)?.also { action(it) }
                false
            }
        } else false
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
            println("quantifier: $this")
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

