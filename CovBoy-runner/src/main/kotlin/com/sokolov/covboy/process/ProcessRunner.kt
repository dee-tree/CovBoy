package com.sokolov.covboy.process

import com.sokolov.covboy.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration

@JvmInline
value class ProcessRunner(val commandWithArgs: List<String>) {
    init {
        check(commandWithArgs.isNotEmpty()) { "Command should not be empty!" }
    }

    suspend fun run(
        timeout: Duration = Duration.INFINITE,
        onComplete: (Process) -> Unit = {},
        onTimeoutExceeded: (Process) -> Unit = {},
        ctx: CoroutineContext = Dispatchers.IO
    ) = withContext(Dispatchers.IO) {

        launch(ctx) {
            val process = ProcessBuilder().command(commandWithArgs).inheritIO().start()
            val finishedOnTimeout = process.waitFor(timeout.inWholeMilliseconds, TimeUnit.MILLISECONDS)

            if (!finishedOnTimeout) {
                logger().warn("Kill process $process on exceeded timeout ($timeout)")
                process.destroyForcibly()
                process.waitFor() // wait destroying stage
                onTimeoutExceeded(process)
            } else {
                onComplete(process)
            }
        }
    }
}

fun List<String>.asProcessRunner() = ProcessRunner(this)
