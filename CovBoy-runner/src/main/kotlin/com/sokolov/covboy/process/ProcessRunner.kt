package com.sokolov.covboy.process

import com.sokolov.covboy.logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.time.Duration

@JvmInline
value class ProcessRunner(val commandWithArgs: List<String>) {
    init {
        check(commandWithArgs.isNotEmpty()) { "Command should not be empty!" }
    }

    suspend fun CoroutineScope.runAsync(
        timeout: Duration = Duration.INFINITE,
    ) = launch {
        val process = ProcessBuilder().command(commandWithArgs).inheritIO().start()
        val finishedOnTimeout = async { process.waitFor(timeout.inWholeMilliseconds, TimeUnit.MILLISECONDS) }

        if (!finishedOnTimeout.await()) {
            logger().warn("Kill process $process on exceeded timeout ($timeout)")
            process.destroyForcibly()
            // TODO: maybe remove waitFor() after process destroyForcibly()
            launch { process.waitFor() }.join() // wait destroying stage
            throw TimeoutException("Process $process exceeded timeout ($timeout)")
        }
    }

    fun run(
        timeout: Duration = Duration.INFINITE,
    ) = also {
        val process = ProcessBuilder().command(commandWithArgs).inheritIO().start()
        val finishedOnTimeout = process.waitFor(timeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)

        if (!finishedOnTimeout) {
            logger().warn("Kill process $process on exceeded timeout ($timeout)")
            process.destroyForcibly()
            // TODO: maybe remove waitFor() after process destroyForcibly()
            process.waitFor() // wait destroying stage
            throw TimeoutException("Process $process exceeded timeout ($timeout)")
        }
    }
}

fun List<String>.asProcessRunner() = ProcessRunner(this)
