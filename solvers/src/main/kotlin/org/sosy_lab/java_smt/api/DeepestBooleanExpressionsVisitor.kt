package org.sosy_lab.java_smt.api

import org.sosy_lab.java_smt.api.visitors.FormulaVisitor
import org.sosy_lab.java_smt.api.visitors.TraversalProcess

class DeepestBooleanExpressionsVisitor<T : Formula>(
    private val formulaManager: FormulaManager,
    val formula: T
) {
    private val deepestBooleans = mutableSetOf<BooleanFormula>()

    fun extractBooleans(clearBefore: Boolean = true): Set<BooleanFormula> {
        if (clearBefore)
            deepestBooleans.clear()

        if (deepestBooleans.isEmpty())
            formulaManager.visitRecursively(formula, visitor)
        return deepestBooleans
    }

    private val visitor = object : FormulaVisitor<TraversalProcess> {
        override fun visitFreeVariable(f: Formula?, name: String?): TraversalProcess {
            if (f is BooleanFormula) {
                deepestBooleans.add(f)
            }
            return TraversalProcess.CONTINUE
        }

        override fun visitBoundVariable(f: Formula?, deBruijnIdx: Int): TraversalProcess {
            if (f is BooleanFormula) {
                deepestBooleans.add(f)
            }
            return TraversalProcess.CONTINUE
        }

        override fun visitConstant(f: Formula?, value: Any?): TraversalProcess {
            if (f is BooleanFormula) {
                deepestBooleans.add(f)
            }
            return TraversalProcess.CONTINUE
        }

        override fun visitFunction(
            f: Formula,
            args: MutableList<Formula>,
            functionDeclaration: FunctionDeclaration<*>
        ): TraversalProcess {
            if (args.any { it is BooleanFormula }) {
                return TraversalProcess.custom(args.filterIsInstance<BooleanFormula>())
            }

            if (f is BooleanFormula) {
                deepestBooleans.add(f)
            }
            return TraversalProcess.SKIP
        }

        override fun visitQuantifier(
            f: BooleanFormula?,
            quantifier: QuantifiedFormulaManager.Quantifier?,
            boundVariables: MutableList<Formula>?,
            body: BooleanFormula?
        ): TraversalProcess {
            if (f is BooleanFormula) {
                deepestBooleans.add(f)
            }
            return TraversalProcess.SKIP
        }
    }

}

fun Formula.getDeepestBooleanExprs(
    fm: FormulaManager
): Set<BooleanFormula> = DeepestBooleanExpressionsVisitor(fm, this).extractBooleans(false)
