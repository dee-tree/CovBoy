package org.sosy_lab.java_smt.api

import java.io.File

// line-based content
// https://github.com/sosy-lab/java-smt/blob/master/src/org/sosy_lab/java_smt/example/FormulaClassifier.java
@Deprecated("use z3fromFile")
fun FormulaManager.readFormulasFromSmtLib(input: File): List<BooleanFormula> {
    val definitions = mutableListOf<String>()
    val formulas = mutableListOf<BooleanFormula>()

    input.forEachLine { line ->
        if (listOf(";", "(push ", "(pop ", "(reset", "(set-logic").any { line.startsWith(it) }) {
        } else if (line.startsWith("(assert ")) {
            val formula = parse(definitions.joinToString(" ") + " " + line)
            formulas.add(formula)
        } else {
            // definition
            definitions.add(line)
        }
    }

    return formulas
}