package com.microsoft.z3


fun Solver.allModels(): Set<Model> = buildSet {

    var i = 0
    val limit = 10
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

fun Optimize.allModels(): Set<Model> = buildSet {
    whileSat { model ->

        add(model)
        val modelExprs = model.constDecls.map { decl ->
            (context.mkConst(decl) eq model.getConstInterp(decl))
        }

        !context.and(*modelExprs.toTypedArray())
    }

}
