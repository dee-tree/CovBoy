package com.sokolov.covboy

import com.sokolov.covboy.solvers.provers.Status
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

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

fun assertEmpty(collection: Collection<*>, message: String? = null) {
    assertTrue(message ?: "expected: empty collection, but it contains ${collection.size} iterms") { collection.isEmpty() }
}

fun assertNotEmpty(collection: Collection<*>, message: String? = null) {
    assertTrue(message ?: "expected: collection containing one or more elements, but it is EMPTY") { collection.isNotEmpty() }
}

fun <T> assertAll(collection: Collection<T>, action: (T) -> Boolean,  message: String? = null) {
    collection.forEach { element ->
        assertTrue(action(element), "expected: all passed, but action failed at element $element")
    }
}

fun <T> assertAny(collection: Collection<T>, action: (T) -> Boolean,  message: String? = null) {
    assertTrue(collection.any(action), "expected: any element is true on action, but actually was nobody")
}
