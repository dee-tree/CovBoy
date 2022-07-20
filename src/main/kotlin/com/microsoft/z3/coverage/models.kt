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