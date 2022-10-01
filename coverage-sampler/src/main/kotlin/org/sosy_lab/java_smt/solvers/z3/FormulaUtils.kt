package org.sosy_lab.java_smt.solvers.z3

import org.sosy_lab.java_smt.api.Formula

fun Formula.isZ3Formula() = this is Z3Formula