package com.sokolov.covboy.prover.model

import org.sosy_lab.java_smt.api.BooleanFormula

interface BoolModelAssignments: ModelAssignments <BooleanFormula> {

//    /*override */fun eval(expr: BooleanFormula): Boolean?
}