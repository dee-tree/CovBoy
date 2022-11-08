package org.sosy_lab.java_smt.solvers.boolector

import org.sosy_lab.java_smt.api.FormulaType.BitvectorType
import java.math.BigInteger

internal class ExtendedBoolectorBitvectorFormulaManager(
    delegate: BoolectorBitvectorFormulaManager,
    booleanFormulaManager: ExtendedBoolectorBooleanFormulaManager,
    private val creator: ExtendedBoolectorFormulaCreator
) : BoolectorBitvectorFormulaManager(creator, booleanFormulaManager) {

    override fun makeBitvectorImpl(pLength: Int, pI: BigInteger): Long {
        return super.makeBitvectorImpl(pLength, pI).also {
            creator.addTermType(it, BitvectorType.getBitvectorTypeWithSize(pLength))
        }
    }

    override fun makeVariableImpl(pLength: Int, pVar: String): Long {
        return super.makeVariableImpl(pLength, pVar).also {
            creator.addTermType(it, BitvectorType.getBitvectorTypeWithSize(pLength))
        }
    }
}