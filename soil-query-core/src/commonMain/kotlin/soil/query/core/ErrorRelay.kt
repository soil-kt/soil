// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * An interface for relaying error information via a back-channel
 * when an error occurs during execution, regardless of Query/Mutation.
 */
interface ErrorRelay {

    /**
     * Sends error information to the relay destination.
     *
     * @param error The error information.
     */
    fun send(error: ErrorRecord)

    /**
     * Provides a Flow for receiving error information.
     */
    fun receiveAsFlow(): Flow<ErrorRecord>

    companion object {

        /**
         * Creates an ErrorRelay that relays error information to one of the destinations.
         *
         * NOTE: Only the latest error information is retained while there are no destinations.
         *
         * @param scope CoroutineScope for the relay.
         * @param policy The relay policy for error information.
         */
        @Suppress("SpellCheckingInspection")
        fun newAnycast(
            scope: CoroutineScope,
            policy: ErrorRelayPolicy = ErrorRelayPolicy.None
        ): ErrorRelay = ErrorRelayBuiltInAnycast(scope, policy)
    }
}

/**
 * A policy for relaying error information.
 */
interface ErrorRelayPolicy {

    /**
     * Determines whether to suppress error information.
     */
    val shouldSuppressError: (ErrorRecord) -> Boolean

    /**
     * Determines whether the error information is equal.
     */
    val areErrorsEqual: (ErrorRecord, ErrorRecord) -> Boolean

    /**
     * A companion object that provides a default implementation of the ErrorRelayPolicy.
     */
    companion object None : ErrorRelayPolicy {
        override val shouldSuppressError: (ErrorRecord) -> Boolean = { false }
        override val areErrorsEqual: (ErrorRecord, ErrorRecord) -> Boolean = { _, _ -> false }
    }
}

private typealias ErrorToken = String

@Suppress("SpellCheckingInspection")
internal class ErrorRelayBuiltInAnycast(
    private val scope: CoroutineScope,
    private val policy: ErrorRelayPolicy
) : ErrorRelay {

    private val mutex = Mutex()
    private val upstream = Channel<ErrorRecord>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val downstream = Channel<ErrorToken>(
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private var current: Pair<ErrorToken, ErrorRecord>? = null

    init {
        scope.launch {
            for (error in upstream) {
                val next = mutex.withLock {
                    val unfulfilled = current
                    if (unfulfilled != null && policy.areErrorsEqual(unfulfilled.second, error)) {
                        return@withLock null
                    }
                    val token = uuid()
                    current = token to error
                    token
                }
                if (next != null) {
                    downstream.send(next)
                }
            }
        }
    }

    override fun send(error: ErrorRecord) {
        if (policy.shouldSuppressError(error)) {
            return
        }
        scope.launch { upstream.send(error) }
    }

    override fun receiveAsFlow(): Flow<ErrorRecord> = flow {
        for (next in downstream) {
            consume(next)?.let { emit(it) }
        }
    }

    private suspend fun consume(next: ErrorToken): ErrorRecord? {
        return mutex.withLock {
            current?.let { (token, info) ->
                if (token == next) {
                    current = null
                    return@withLock info
                }
            }
            null
        }
    }
}
