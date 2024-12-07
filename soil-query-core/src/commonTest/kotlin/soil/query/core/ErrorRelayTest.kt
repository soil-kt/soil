// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import soil.query.QueryId
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ErrorRelayTest : UnitTest() {

    @Test
    fun testAnycast_one() = runTest {
        val relay = ErrorRelay.newAnycast(
            scope = backgroundScope
        )

        val err1 = ErrorRecord(
            exception = RuntimeException("Error 1"),
            keyId = QueryId<String>(namespace = "test"),
            marker = Marker.None
        )

        relay.send(err1)
        yield()

        relay.receiveAsFlow().test {
            assertEquals(err1, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun testAnycast_many() = runTest {
        val relay = ErrorRelay.newAnycast(
            scope = backgroundScope
        )

        val err1 = ErrorRecord(
            exception = RuntimeException("Error 1"),
            keyId = QueryId<String>(namespace = "test"),
            marker = Marker.None
        )

        relay.send(err1)
        yield()

        val flow1 = relay.receiveAsFlow()
        val flow2 = relay.receiveAsFlow()
        merge(flow1, flow2).test {
            assertEquals(err1, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun testAnycast_dropOldest() = runTest {
        val relay = ErrorRelay.newAnycast(
            scope = backgroundScope
        )

        val err1 = ErrorRecord(
            exception = RuntimeException("Error 1"),
            keyId = QueryId<String>(namespace = "test"),
            marker = Marker.None
        )

        relay.send(err1)
        runCurrent()

        val err2 = ErrorRecord(
            exception = RuntimeException("Error 2"),
            keyId = QueryId<String>(namespace = "test"),
            marker = Marker.None
        )

        relay.send(err2)
        runCurrent()

        relay.receiveAsFlow().test {
            assertEquals(err2, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun testAnycastPolicy_shouldSuppressError() = runTest {
        val relay = ErrorRelay.newAnycast(
            scope = backgroundScope,
            policy = TestErrorRelayPolicy(
                shouldSuppressError = { it.marker == ErrorMarker.Suppress }
            )
        )

        val err1 = ErrorRecord(
            exception = RuntimeException("Error 1"),
            keyId = QueryId<String>(namespace = "test"),
            marker = ErrorMarker.Suppress
        )

        relay.send(err1)
        runCurrent()

        relay.receiveAsFlow().test {
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun testAnycastPolicy_areErrorsEqual() = runTest {
        val relay = ErrorRelay.newAnycast(
            scope = backgroundScope,
            policy = TestErrorRelayPolicy(
                areErrorsEqual = { a, b -> a.keyId == b.keyId }
            )
        )

        val err1 = ErrorRecord(
            exception = RuntimeException("Error 1"),
            keyId = QueryId<String>(namespace = "test"),
            marker = ErrorMarker.Suppress
        )

        relay.send(err1)
        runCurrent()

        val err2 = ErrorRecord(
            exception = RuntimeException("Error 2"),
            keyId = QueryId<String>(namespace = "test"),
            marker = Marker.None
        )

        relay.send(err2)
        runCurrent()

        relay.receiveAsFlow().test {
            assertEquals(err1, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    class TestErrorRelayPolicy(
        override val shouldSuppressError: (ErrorRecord) -> Boolean = ErrorRelayPolicy.None.shouldSuppressError,
        override val areErrorsEqual: (ErrorRecord, ErrorRecord) -> Boolean = ErrorRelayPolicy.None.areErrorsEqual
    ) : ErrorRelayPolicy

    sealed class ErrorMarker : Marker.Element {
        override val key: Marker.Key<*>
            get() = Key

        companion object Key : Marker.Key<ErrorMarker>

        data object Suppress : ErrorMarker()
    }
}
