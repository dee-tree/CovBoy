package com.sokolov.covboy.process

import com.sokolov.covboy.getOsName
import com.sokolov.covboy.isLinux
import com.sokolov.covboy.isWindows
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.concurrent.Executors
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

class ProcessRunnerTest {

    @Test
    fun testProcessRunTimeout() {
        val command = delayCommand(10)
        var timeoutExceededCallbackCalled = false
        val dispatcher = Executors.newFixedThreadPool(1).asCoroutineDispatcher()

        runBlocking {
            var processPushed = false

            command.asProcessRunner().run(
                timeout = 1.seconds,
                // pass coroutineContext to avoid new scope creation
                ctx = coroutineContext + dispatcher,

                onComplete = {
                    assertTrue { false } // assume unachievable state
                },

                onTimeoutExceeded = {
                    assertTrue { processPushed }
                    assertFalse { it.isAlive }
                    timeoutExceededCallbackCalled = true
                }
            )

            // executes before process completion
            assertFalse { processPushed }
            processPushed = true
        }

        // executes after process killed
        assertTrue { timeoutExceededCallbackCalled }
        dispatcher.close()
    }

    @Test
    fun testProcessRunComplete() {
        val command = delayCommand(1)
        var onCompleteCallbackCalled = false
        val dispatcher = Executors.newFixedThreadPool(1).asCoroutineDispatcher()

        runBlocking {
            var processPushed = false

            command.asProcessRunner().run(
                timeout = 5.seconds,
                ctx = coroutineContext + dispatcher,

                onComplete = {
                    assertTrue { processPushed }
                    assertFalse { it.isAlive }
                    onCompleteCallbackCalled = true
                },

                onTimeoutExceeded = {
                    assertTrue { false } // assume unachievable state
                }
            )

            // executes before process completion
            assertFalse { processPushed }
            processPushed = true
        }

        // executes after process completion
        assertTrue { onCompleteCallbackCalled }
        dispatcher.close()
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 5, 10, 20])
    fun testParallelProcessRunTimeout(processesCount: Int) {
        val command = delayCommand(5)
        var timeoutExceededCallbackCalledTimes = 0
        val dispatcher = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors())
            .asCoroutineDispatcher()

        runBlocking {
            var pushedProcessesCount = 0

            val processJobs = (1..processesCount).mapIndexed { iter, i ->
                command.asProcessRunner().run(
                    timeout = 1.seconds,
                    ctx = coroutineContext + dispatcher,

                    onComplete = {
                        assertFalse { true } // assume unachievable state
                    },

                    onTimeoutExceeded = {
                        assertEquals(processesCount, pushedProcessesCount)
                        assertFalse { it.isAlive }
                        timeoutExceededCallbackCalledTimes++
                    }
                ).also {
                    // executes before process completion, but after its start
                    assertTrue { pushedProcessesCount < processesCount }
                    pushedProcessesCount++
                }

            }

            // executes before process completion, but after all them run
            assertEquals(processesCount, pushedProcessesCount)
            assertEquals(0, timeoutExceededCallbackCalledTimes)
        }
        assertEquals(processesCount, timeoutExceededCallbackCalledTimes)
        dispatcher.close()
    }

    private fun delayCommand(seconds: Int) = when {
        isLinux -> "sleep $seconds"
        isWindows -> "timeout $seconds"
        else -> TODO("add command for OS: ${getOsName()}")
    }.split(' ')
}
