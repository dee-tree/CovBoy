package com.sokolov.covboy.prover.model

import org.sosy_lab.java_smt.api.Formula

interface ModelAssignments <T : Formula> {
    fun evaluate(expr: T): T?
//    fun eval(expr: T): Any?
}