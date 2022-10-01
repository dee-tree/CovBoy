package org.sosy_lab.java_smt.solvers.boolector

import org.sosy_lab.java_smt.api.Formula

fun Formula.isBoolectorFormula() = this is BoolectorFormula