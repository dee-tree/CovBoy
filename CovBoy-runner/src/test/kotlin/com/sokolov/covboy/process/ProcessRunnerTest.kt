package com.sokolov.covboy.process

import com.sokolov.covboy.getOsName
import com.sokolov.covboy.isLinux
import com.sokolov.covboy.isWindows
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.util.concurrent.Executors
import java.util.concurrent.TimeoutException
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class ProcessRunnerTest {

    @Test
    @OptIn(ExperimentalTime::class)
    fun testProcessRunTimeout() {
        assumeTrue("windows" !in getOsName())
        val delayTime = 10.seconds
        val timeout = 1.seconds
        val command = delayCommand(delayTime.inWholeSeconds.toInt())
        val dispatcher = Executors.newFixedThreadPool(1).asCoroutineDispatcher()

        runBlocking {
            measureTime {
                assertThrows<TimeoutException> { command.asProcessRunner().run(timeout = timeout) }
            }.also {
                assertTrue { timeout <= it && it < delayTime }
            }
        }

        dispatcher.close()
    }

    @Test
    @OptIn(ExperimentalTime::class)
    fun testProcessRunComplete() {
        assumeTrue("windows" !in getOsName())

        val delayTime = 1.seconds
        val timeout = 5.seconds
        val command = delayCommand(delayTime.inWholeSeconds.toInt())
        val dispatcher = Executors.newFixedThreadPool(1).asCoroutineDispatcher()

        runBlocking {
            measureTime {
                assertDoesNotThrow { command.asProcessRunner().run(timeout = timeout) }
            }.also {
                assertTrue { delayTime <= it && it < timeout * 2 }
            }
        }

        dispatcher.close()
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 4, 8, 12])
    @OptIn(ExperimentalTime::class)
    fun testParallelProcessRunTimeout(processesCount: Int) {
        assumeTrue("windows" !in getOsName())

        assumeTrue(processesCount <= Runtime.getRuntime().availableProcessors())

        val delayTime = 10.seconds
        val timeout = 5.seconds
        val command = delayCommand(delayTime.inWholeSeconds.toInt())
        val dispatcher = Executors
            .newFixedThreadPool(processesCount)
            .asCoroutineDispatcher()

        runBlocking {
            measureTime {
                (1..processesCount).mapIndexed { iter, i ->
                    withContext(dispatcher) {
                        measureTime {
                            assertThrows<TimeoutException> { command.asProcessRunner().run(timeout = timeout) }
                        }.also { assertTrue { timeout <= it && it < delayTime } }
                    }
                }

            }.also {
                assertTrue { it < timeout * processesCount }
            }
        }

        dispatcher.close()
    }

    private fun delayCommand(seconds: Int) = when {
        isLinux -> "sleep $seconds"
        else -> TODO("add command for OS: ${getOsName()}")
    }.split(' ')
}
