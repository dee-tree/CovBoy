package org.sosy_lab.java_smt.basicimpl

import org.sosy_lab.java_smt.api.BitvectorFormula
import org.sosy_lab.java_smt.api.BitvectorFormulaManager

fun BitvectorFormulaManager.and(args: List<BitvectorFormula>): BitvectorFormula = when (args.size) {
    0 -> error("Empty args list in bv and!")
    1 -> args.first()
    2 -> and(args[0], args[1])
    else -> {
        args.subList(1, args.size).fold(args.first()) { acc, curr -> and(curr, acc) }
    }
}

fun BitvectorFormulaManager.or(args: List<BitvectorFormula>): BitvectorFormula = when (args.size) {
    0 -> error("Empty args list in bv and!")
    1 -> args.first()
    2 -> or(args[0], args[1])
    else -> {
        args.subList(1, args.size).fold(args.first()) { acc, curr -> or(curr, acc) }
    }
}

fun BitvectorFormulaManager.add(args: List<BitvectorFormula>): BitvectorFormula = when (args.size) {
    0 -> error("Empty args list in bv and!")
    1 -> args.first()
    2 -> add(args[0], args[1])
    else -> {
        args.subList(1, args.size).fold(args.first()) { acc, curr -> add(curr, acc) }
    }
}