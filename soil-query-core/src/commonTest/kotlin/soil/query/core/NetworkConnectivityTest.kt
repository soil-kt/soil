// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class NetworkConnectivityTest : UnitTest() {

    @Test
    fun testObserveOnNetworkReconnect() = runTest {
        val mockConnectivity = MockConnectivity()
        val flow = flow {
            observeOnNetworkReconnect(
                networkConnectivity = mockConnectivity,
                networkResumeAfterDelay = 1.seconds,
                collector = this
            )
        }

        flow.test {
            mockConnectivity.notify(NetworkConnectivityEvent.Lost)
            mockConnectivity.notify(NetworkConnectivityEvent.Available)
            advanceTimeBy(1.seconds)
            awaitItem()
        }
    }

    @Test
    fun testObserveOnNetworkReconnect_noEventIfNoDisconnection() = runTest {
        val mockConnectivity = MockConnectivity()
        val flow = flow {
            observeOnNetworkReconnect(
                networkConnectivity = mockConnectivity,
                networkResumeAfterDelay = 1.seconds,
                collector = this
            )
        }

        flow.test {
            mockConnectivity.notify(NetworkConnectivityEvent.Available)
            advanceTimeBy(2.seconds)
            expectNoEvents()
        }
    }

    @Test
    fun testObserveOnNetworkReconnect_multipleReconnects() = runTest {
        val mockConnectivity = MockConnectivity()
        val flow = flow {
            observeOnNetworkReconnect(
                networkConnectivity = mockConnectivity,
                networkResumeAfterDelay = 1.seconds,
                collector = this
            )
        }

        flow.test {
            mockConnectivity.notify(NetworkConnectivityEvent.Lost)
            mockConnectivity.notify(NetworkConnectivityEvent.Available)
            advanceTimeBy(1.seconds)
            awaitItem()

            mockConnectivity.notify(NetworkConnectivityEvent.Lost)
            mockConnectivity.notify(NetworkConnectivityEvent.Available)
            advanceTimeBy(1.seconds)
            awaitItem()
        }
    }

    @Test
    fun testObserveOnNetworkReconnect_noEventIfStaysDisconnected() = runTest {
        val mockConnectivity = MockConnectivity()
        val flow = flow {
            observeOnNetworkReconnect(
                networkConnectivity = mockConnectivity,
                networkResumeAfterDelay = 1.seconds,
                collector = this
            )
        }

        flow.test {
            mockConnectivity.notify(NetworkConnectivityEvent.Lost)
            advanceTimeBy(5.seconds)
            expectNoEvents()
        }
    }

    @Test
    fun testObserveOnNetworkReconnect_delayIsRespected() = runTest {
        val mockConnectivity = MockConnectivity()
        val flow = flow {
            observeOnNetworkReconnect(
                networkConnectivity = mockConnectivity,
                networkResumeAfterDelay = 3.seconds,
                collector = this
            )
        }

        flow.test {
            mockConnectivity.notify(NetworkConnectivityEvent.Lost)
            mockConnectivity.notify(NetworkConnectivityEvent.Available)

            advanceTimeBy(2.seconds)
            expectNoEvents()

            advanceTimeBy(1.seconds)
            awaitItem()
        }
    }

    private class MockConnectivity : AbstractNotifier<NetworkConnectivityEvent>(), NetworkConnectivity
}
