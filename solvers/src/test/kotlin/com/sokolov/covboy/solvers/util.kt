package com.sokolov.covboy.solvers

import com.sokolov.covboy.solvers.provers.Status
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

fun assertSat(action: () -> Status) {
    val status = action()
    assertEquals(Status.SAT, status, "Expected SAT, but was: $status")
}

fun assertUnsat(action: () -> Status) {
    val status = action()
    assertEquals(Status.UNSAT, status, "Expected UNSAT, but was: $status")
}

fun assertUnknown(action: () -> Status) {
    val status = action()
    assertEquals(Status.UNKNOWN, status, "Expected UNKNOWN, but was: $status")
}

fun <T> assertNotContains(collection: Collection<T>, item: T, message: String? = null) {
    collection.forEach { element ->
        assertNotEquals(element, item, message ?: "expected: collection does not contain <$item>, but was")
    }
}