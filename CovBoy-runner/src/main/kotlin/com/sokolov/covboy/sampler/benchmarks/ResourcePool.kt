package com.sokolov.covboy.sampler.benchmarks

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking

class ResourcePool<T>(initialResources: List<T>) : AutoCloseable {

    private val channel = Channel<T>(Channel.UNLIMITED)

    init {
        for (res in initialResources) {
            channel.trySend(res)
        }
    }

    suspend fun <R> borrow(action: (T) -> R): R {
        val borrowed = channel.receive()
        try {
            return action(borrowed)
        } finally {
            channel.trySend(borrowed)
        }
    }

    override fun close() {
        runBlocking {
            channel.close()
            for (res in channel)
                if (res is AutoCloseable)
                    res.close()
        }
    }
}
