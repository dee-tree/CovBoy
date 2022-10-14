package com.sokolov.covboy.prover

import java.util.concurrent.CancellationException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

fun <T> withTimeout(timeoutMillis: Long, action: () -> T, onTimeout: () -> T): T {
    val executor = Executors.newScheduledThreadPool(2)
    val future = executor.submit(action)

    val cancelAction = {
        future.cancel(true)
        onTimeout()
    }

    val cancellation = executor.schedule(cancelAction, timeoutMillis, TimeUnit.MILLISECONDS)
    executor.shutdown()

    return try {
        future.get()
    } catch (e: CancellationException) {
        cancellation.get()
    }
}
