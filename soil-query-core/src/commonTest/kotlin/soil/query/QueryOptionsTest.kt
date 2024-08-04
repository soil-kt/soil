// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.testing.UnitTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.time.Duration.Companion.seconds

class QueryOptionsTest : UnitTest() {

    @Test
    fun factory_default() {
        val actual = QueryOptions()
        assertEquals(QueryOptions.Default.staleTime, actual.staleTime)
        assertEquals(QueryOptions.Default.gcTime, actual.gcTime)
        assertEquals(QueryOptions.Default.prefetchWindowTime, actual.prefetchWindowTime)
        assertEquals(QueryOptions.Default.pauseDurationAfter, actual.pauseDurationAfter)
        assertEquals(QueryOptions.Default.revalidateOnReconnect, actual.revalidateOnReconnect)
        assertEquals(QueryOptions.Default.revalidateOnFocus, actual.revalidateOnFocus)
        assertEquals(QueryOptions.Default.onError, actual.onError)
        assertEquals(QueryOptions.Default.keepAliveTime, actual.keepAliveTime)
        assertEquals(QueryOptions.Default.logger, actual.logger)
        assertEquals(QueryOptions.Default.shouldRetry, actual.shouldRetry)
        assertEquals(QueryOptions.Default.retryCount, actual.retryCount)
        assertEquals(QueryOptions.Default.retryInitialInterval, actual.retryInitialInterval)
        assertEquals(QueryOptions.Default.retryMaxInterval, actual.retryMaxInterval)
        assertEquals(QueryOptions.Default.retryMultiplier, actual.retryMultiplier)
        assertEquals(QueryOptions.Default.retryRandomizationFactor, actual.retryRandomizationFactor)
        assertEquals(QueryOptions.Default.retryRandomizer, actual.retryRandomizer)
    }

    @Test
    fun factory_specifyingArguments() {
        val actual = QueryOptions(
            staleTime = 1000.seconds,
            gcTime = 2000.seconds,
            prefetchWindowTime = 3000.seconds,
            pauseDurationAfter = { null },
            revalidateOnReconnect = false,
            revalidateOnFocus = false,
            onError = { },
            keepAliveTime = 4000.seconds,
            logger = { _ -> },
            shouldRetry = { _ -> true },
            retryCount = 999,
            retryInitialInterval = 5000.seconds,
            retryMaxInterval = 6000.seconds,
            retryMultiplier = 0.1,
            retryRandomizationFactor = 0.01,
            retryRandomizer = Random(999)
        )
        assertNotEquals(QueryOptions.Default.staleTime, actual.staleTime)
        assertNotEquals(QueryOptions.Default.gcTime, actual.gcTime)
        assertNotEquals(QueryOptions.Default.prefetchWindowTime, actual.prefetchWindowTime)
        assertNotEquals(QueryOptions.Default.pauseDurationAfter, actual.pauseDurationAfter)
        assertNotEquals(QueryOptions.Default.revalidateOnReconnect, actual.revalidateOnReconnect)
        assertNotEquals(QueryOptions.Default.revalidateOnFocus, actual.revalidateOnFocus)
        assertNotEquals(QueryOptions.Default.onError, actual.onError)
        assertNotEquals(QueryOptions.Default.keepAliveTime, actual.keepAliveTime)
        assertNotEquals(QueryOptions.Default.logger, actual.logger)
        assertNotEquals(QueryOptions.Default.shouldRetry, actual.shouldRetry)
        assertNotEquals(QueryOptions.Default.retryCount, actual.retryCount)
        assertNotEquals(QueryOptions.Default.retryInitialInterval, actual.retryInitialInterval)
        assertNotEquals(QueryOptions.Default.retryMaxInterval, actual.retryMaxInterval)
        assertNotEquals(QueryOptions.Default.retryMultiplier, actual.retryMultiplier)
        assertNotEquals(QueryOptions.Default.retryRandomizationFactor, actual.retryRandomizationFactor)
        assertNotEquals(QueryOptions.Default.retryRandomizer, actual.retryRandomizer)
    }

    @Test
    fun copy_default() {
        val actual = QueryOptions.Default.copy()
        assertEquals(QueryOptions.Default.staleTime, actual.staleTime)
        assertEquals(QueryOptions.Default.gcTime, actual.gcTime)
        assertEquals(QueryOptions.Default.prefetchWindowTime, actual.prefetchWindowTime)
        assertEquals(QueryOptions.Default.pauseDurationAfter, actual.pauseDurationAfter)
        assertEquals(QueryOptions.Default.revalidateOnReconnect, actual.revalidateOnReconnect)
        assertEquals(QueryOptions.Default.revalidateOnFocus, actual.revalidateOnFocus)
        assertEquals(QueryOptions.Default.onError, actual.onError)
        assertEquals(QueryOptions.Default.keepAliveTime, actual.keepAliveTime)
        assertEquals(QueryOptions.Default.logger, actual.logger)
        assertEquals(QueryOptions.Default.shouldRetry, actual.shouldRetry)
        assertEquals(QueryOptions.Default.retryCount, actual.retryCount)
        assertEquals(QueryOptions.Default.retryInitialInterval, actual.retryInitialInterval)
        assertEquals(QueryOptions.Default.retryMaxInterval, actual.retryMaxInterval)
        assertEquals(QueryOptions.Default.retryMultiplier, actual.retryMultiplier)
        assertEquals(QueryOptions.Default.retryRandomizationFactor, actual.retryRandomizationFactor)
        assertEquals(QueryOptions.Default.retryRandomizer, actual.retryRandomizer)
    }

    @Test
    fun copy_override() {
        val actual = QueryOptions.copy(
            staleTime = 1000.seconds,
            gcTime = 2000.seconds,
            prefetchWindowTime = 3000.seconds,
            pauseDurationAfter = { null },
            revalidateOnReconnect = false,
            revalidateOnFocus = false,
            onError = { },
            keepAliveTime = 4000.seconds,
            logger = { _ -> },
            shouldRetry = { _ -> true },
            retryCount = 999,
            retryInitialInterval = 5000.seconds,
            retryMaxInterval = 6000.seconds,
            retryMultiplier = 0.1,
            retryRandomizationFactor = 0.01,
            retryRandomizer = Random(999)
        )
        assertNotEquals(QueryOptions.Default.staleTime, actual.staleTime)
        assertNotEquals(QueryOptions.Default.gcTime, actual.gcTime)
        assertNotEquals(QueryOptions.Default.prefetchWindowTime, actual.prefetchWindowTime)
        assertNotEquals(QueryOptions.Default.pauseDurationAfter, actual.pauseDurationAfter)
        assertNotEquals(QueryOptions.Default.revalidateOnReconnect, actual.revalidateOnReconnect)
        assertNotEquals(QueryOptions.Default.revalidateOnFocus, actual.revalidateOnFocus)
        assertNotEquals(QueryOptions.Default.onError, actual.onError)
        assertNotEquals(QueryOptions.Default.keepAliveTime, actual.keepAliveTime)
        assertNotEquals(QueryOptions.Default.logger, actual.logger)
        assertNotEquals(QueryOptions.Default.shouldRetry, actual.shouldRetry)
        assertNotEquals(QueryOptions.Default.retryCount, actual.retryCount)
        assertNotEquals(QueryOptions.Default.retryInitialInterval, actual.retryInitialInterval)
        assertNotEquals(QueryOptions.Default.retryMaxInterval, actual.retryMaxInterval)
        assertNotEquals(QueryOptions.Default.retryMultiplier, actual.retryMultiplier)
        assertNotEquals(QueryOptions.Default.retryRandomizationFactor, actual.retryRandomizationFactor)
        assertNotEquals(QueryOptions.Default.retryRandomizer, actual.retryRandomizer)
    }
}
