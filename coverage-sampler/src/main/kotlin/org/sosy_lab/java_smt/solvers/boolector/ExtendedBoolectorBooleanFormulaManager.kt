package org.sosy_lab.java_smt.solvers.boolector

import org.sosy_lab.java_smt.api.FormulaType

class ExtendedBoolectorBooleanFormulaManager(
    delegate: BoolectorBooleanFormulaManager,
    private val creator: ExtendedBoolectorFormulaCreator
) : BoolectorBooleanFormulaManager(creator) {

    override fun makeVariableImpl(varName: String): Long {
        return super.makeVariableImpl(varName).also {
            creator.addTermType(it, FormulaType.BooleanType)
        }
    }

    override fun makeBooleanImpl(pValue: Boolean): Long {
        return super.makeBooleanImpl(pValue).also {
            creator.addTermType(it, FormulaType.BooleanType)
        }
    }

    override fun not(pTerm: Long): Long {
        return super.not(pTerm).also {
            creator.addTermType(it, FormulaType.BooleanType)
        }
    }

    override fun and(pTerm1: Long, pTerm2: Long): Long {
        return super.and(pTerm1, pTerm2).also {
            creator.addTermType(it, FormulaType.BooleanType)
        }
    }

    override fun or(pTerm1: Long, pTerm2: Long): Long {
        return super.or(pTerm1, pTerm2).also {
            creator.addTermType(it, FormulaType.BooleanType)
        }
    }

    override fun xor(pTerm1: Long, pTerm2: Long): Long {
        return super.xor(pTerm1, pTerm2).also {
            creator.addTermType(it, FormulaType.BooleanType)
        }
    }

    override fun equivalence(pTerm1: Long, pTerm2: Long): Long {
        return super.equivalence(pTerm1, pTerm2).also {
            creator.addTermType(it, FormulaType.BooleanType)
        }
    }

    override fun ifThenElse(pCond: Long, pF1: Long, pF2: Long): Long {
        return super.ifThenElse(pCond, pF1, pF2).also {
            creator.addTermType(it, creator.getFormulaType(pF1))
        }
    }
}