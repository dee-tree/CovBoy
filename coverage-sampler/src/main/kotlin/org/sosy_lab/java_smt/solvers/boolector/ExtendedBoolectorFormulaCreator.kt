package org.sosy_lab.java_smt.solvers.boolector

import org.sosy_lab.java_smt.api.FormulaType

class ExtendedBoolectorFormulaCreator(
    delegate: BoolectorFormulaCreator
) : BoolectorFormulaCreator(delegate.env) {

    private val termTypeCache = mutableMapOf<Long, FormulaType<*>>()

    fun addTermType(pTerm: Long, type: FormulaType<*>) {
//        if (pTerm !in termTypeCache.keys) {
        termTypeCache[pTerm] = type
//        }
    }

    override fun getFormulaType(pFormula: Long): FormulaType<*> {
        termTypeCache[pFormula]?.let { return it }

        var sort = BtorJNI.boolector_get_sort(env, pFormula)
        if (!BtorJNI.boolector_is_array_sort(env, sort) && !BtorJNI.boolector_is_bitvec_sort(
                env,
                sort
            ) && BtorJNI.boolector_is_fun_sort(env, sort)
        ) {
            sort = BtorJNI.boolector_fun_get_codomain_sort(env, pFormula)
        }

        return getFormulaTypeFromSortAndFormula(pFormula, sort)
    }

    private fun getFormulaTypeFromSortAndFormula(pFormula: Long, pSort: Long): FormulaType<*> {
        val width: Int

        return if (BtorJNI.boolector_is_array_sort(env, pSort)) {
            width = BtorJNI.boolector_get_index_width(env, pFormula)
            val elementWidth = BtorJNI.boolector_get_width(env, pFormula)

            FormulaType.getArrayType(
                FormulaType.getBitvectorTypeWithSize(width),
                FormulaType.getBitvectorTypeWithSize(elementWidth)
            )
        } else if (BtorJNI.boolector_is_bitvec_sort(env, pSort)) {
            width = BtorJNI.boolector_bitvec_sort_get_width(env, pSort)

            if (width == 1) FormulaType.BooleanType else FormulaType.getBitvectorTypeWithSize(width)
        } else {
            throw IllegalArgumentException("Unknown formula type for $pFormula")
        }
    }

    /*
     This is one-size bitvector shit

    public FormulaType<?> getFormulaType(Long pFormula) {
        long sort = BtorJNI.boolector_get_sort((Long)this.getEnv(), pFormula);
        if (!BtorJNI.boolector_is_array_sort((Long)this.getEnv(), sort) && !BtorJNI.boolector_is_bitvec_sort((Long)this.getEnv(), sort) && BtorJNI.boolector_is_fun_sort((Long)this.getEnv(), sort)) {
            sort = BtorJNI.boolector_fun_get_codomain_sort((Long)this.getEnv(), pFormula);
        }

        return this.getFormulaTypeFromSortAndFormula(pFormula, sort);
    }

    private FormulaType<?> getFormulaTypeFromSortAndFormula(Long pFormula, Long sort) {
        int width;
        if (BtorJNI.boolector_is_array_sort((Long)this.getEnv(), sort)) {
            width = BtorJNI.boolector_get_index_width((Long)this.getEnv(), pFormula);
            int elementWidth = BtorJNI.boolector_get_width((Long)this.getEnv(), pFormula);
            return FormulaType.getArrayType(FormulaType.getBitvectorTypeWithSize(width), FormulaType.getBitvectorTypeWithSize(elementWidth));
        } else if (BtorJNI.boolector_is_bitvec_sort((Long)this.getEnv(), sort)) {
            width = BtorJNI.boolector_bitvec_sort_get_width((Long)this.getEnv(), sort);
            return (FormulaType)(width == 1 ? FormulaType.BooleanType : FormulaType.getBitvectorTypeWithSize(width));
        } else {
            throw new IllegalArgumentException("Unknown formula type for " + pFormula);
        }
    }

     */
}