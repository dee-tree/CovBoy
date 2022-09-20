package com.microsoft.z3

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun <T> withContext(configuration: Map<String, String> = emptyMap(), action: Context.() -> T): T {
    contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
    val context = Context(configuration)
    return action(context).also { context.close() }
}