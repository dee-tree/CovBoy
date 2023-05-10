package com.sokolov.covboy.process

import com.sokolov.covboy.logger
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.time.Duration

@JvmInline
value class ProcessRunner(val commandWithArgs: List<String>) {
    init {
        check(commandWithArgs.isNotEmpty()) { "Command should not be empty!" }
    }

    suspend fun run(
        timeout: Duration = Duration.INFINITE,
    ) = coroutineScope {
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
}

fun List<String>.asProcessRunner() = ProcessRunner(this)
