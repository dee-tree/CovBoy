package com.sokolov.covboy.prover.assertions

import com.sokolov.covboy.prover.Assertion

interface AssertionListener {
    fun onAssertionEnabled(assertion: Assertion) = Unit
    fun onAssertionDisabled(assertion: Assertion) = Unit
}