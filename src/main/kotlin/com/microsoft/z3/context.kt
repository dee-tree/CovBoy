package com.microsoft.z3

inline fun <T> withContext(configuration: Map<String, String> = emptyMap(), action: Context.() -> T) = Context(configuration).apply {
    action(this)
}