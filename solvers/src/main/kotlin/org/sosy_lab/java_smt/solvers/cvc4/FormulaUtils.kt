package org.sosy_lab.java_smt.solvers.cvc4

import org.sosy_lab.java_smt.api.Formula

fun Formula.isCVC4Formula() = this is CVC4Formula