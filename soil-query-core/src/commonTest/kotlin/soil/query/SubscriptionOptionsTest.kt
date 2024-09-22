// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.testing.UnitTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.time.Duration.Companion.seconds

class SubscriptionOptionsTest : UnitTest() {

    @Test
    fun factory_default() {
        val actual = SubscriptionOptions()
        assertEquals(SubscriptionOptions.Default.gcTime, actual.gcTime)
        assertEquals(SubscriptionOptions.Default.errorEquals, actual.errorEquals)
        assertEquals(SubscriptionOptions.Default.onError, actual.onError)
        assertEquals(SubscriptionOptions.Default.shouldSuppressErrorRelay, actual.shouldSuppressErrorRelay)
        assertEquals(SubscriptionOptions.Default.keepAliveTime, actual.keepAliveTime)
        assertEquals(SubscriptionOptions.Default.logger, actual.logger)
        assertEquals(SubscriptionOptions.Default.shouldRetry, actual.shouldRetry)
        assertEquals(SubscriptionOptions.Default.retryCount, actual.retryCount)
        assertEquals(SubscriptionOptions.Default.retryInitialInterval, actual.retryInitialInterval)
        assertEquals(SubscriptionOptions.Default.retryMaxInterval, actual.retryMaxInterval)
        assertEquals(SubscriptionOptions.Default.retryMultiplier, actual.retryMultiplier)
        assertEquals(SubscriptionOptions.Default.retryRandomizationFactor, actual.retryRandomizationFactor)
        assertEquals(SubscriptionOptions.Default.retryRandomizer, actual.retryRandomizer)
    }

    @Test
    fun factory_factory_specifyingArguments() {
        val actual = SubscriptionOptions(
            gcTime = 1000.seconds,
            errorEquals = { _, _ -> true },
            onError = { _, _ -> },
            shouldSuppressErrorRelay = { _, _ -> true },
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
        assertNotEquals(SubscriptionOptions.Default.gcTime, actual.gcTime)
        assertNotEquals(SubscriptionOptions.Default.errorEquals, actual.errorEquals)
        assertNotEquals(SubscriptionOptions.Default.onError, actual.onError)
        assertNotEquals(SubscriptionOptions.Default.shouldSuppressErrorRelay, actual.shouldSuppressErrorRelay)
        assertNotEquals(SubscriptionOptions.Default.keepAliveTime, actual.keepAliveTime)
        assertNotEquals(SubscriptionOptions.Default.logger, actual.logger)
        assertNotEquals(SubscriptionOptions.Default.shouldRetry, actual.shouldRetry)
        assertNotEquals(SubscriptionOptions.Default.retryCount, actual.retryCount)
        assertNotEquals(SubscriptionOptions.Default.retryInitialInterval, actual.retryInitialInterval)
        assertNotEquals(SubscriptionOptions.Default.retryMaxInterval, actual.retryMaxInterval)
        assertNotEquals(SubscriptionOptions.Default.retryMultiplier, actual.retryMultiplier)
        assertNotEquals(SubscriptionOptions.Default.retryRandomizationFactor, actual.retryRandomizationFactor)
        assertNotEquals(SubscriptionOptions.Default.retryRandomizer, actual.retryRandomizer)
    }

    @Test
    fun copy_default() {
        val actual = SubscriptionOptions.copy()
        assertEquals(SubscriptionOptions.Default.gcTime, actual.gcTime)
        assertEquals(SubscriptionOptions.Default.errorEquals, actual.errorEquals)
        assertEquals(SubscriptionOptions.Default.onError, actual.onError)
        assertEquals(SubscriptionOptions.Default.shouldSuppressErrorRelay, actual.shouldSuppressErrorRelay)
        assertEquals(SubscriptionOptions.Default.keepAliveTime, actual.keepAliveTime)
        assertEquals(SubscriptionOptions.Default.logger, actual.logger)
        assertEquals(SubscriptionOptions.Default.shouldRetry, actual.shouldRetry)
        assertEquals(SubscriptionOptions.Default.retryCount, actual.retryCount)
        assertEquals(SubscriptionOptions.Default.retryInitialInterval, actual.retryInitialInterval)
        assertEquals(SubscriptionOptions.Default.retryMaxInterval, actual.retryMaxInterval)
        assertEquals(SubscriptionOptions.Default.retryMultiplier, actual.retryMultiplier)
        assertEquals(SubscriptionOptions.Default.retryRandomizationFactor, actual.retryRandomizationFactor)
        assertEquals(SubscriptionOptions.Default.retryRandomizer, actual.retryRandomizer)
    }

    @Test
    fun copy_override() {
        val actual = SubscriptionOptions.copy(
            gcTime = 1000.seconds,
            errorEquals = { _, _ -> true },
            onError = { _, _ -> },
            shouldSuppressErrorRelay = { _, _ -> true },
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

        assertNotEquals(SubscriptionOptions.Default.gcTime, actual.gcTime)
        assertNotEquals(SubscriptionOptions.Default.errorEquals, actual.errorEquals)
        assertNotEquals(SubscriptionOptions.Default.onError, actual.onError)
        assertNotEquals(SubscriptionOptions.Default.shouldSuppressErrorRelay, actual.shouldSuppressErrorRelay)
        assertNotEquals(SubscriptionOptions.Default.keepAliveTime, actual.keepAliveTime)
        assertNotEquals(SubscriptionOptions.Default.logger, actual.logger)
        assertNotEquals(SubscriptionOptions.Default.shouldRetry, actual.shouldRetry)
        assertNotEquals(SubscriptionOptions.Default.retryCount, actual.retryCount)
        assertNotEquals(SubscriptionOptions.Default.retryInitialInterval, actual.retryInitialInterval)
        assertNotEquals(SubscriptionOptions.Default.retryMaxInterval, actual.retryMaxInterval)
        assertNotEquals(SubscriptionOptions.Default.retryMultiplier, actual.retryMultiplier)
        assertNotEquals(SubscriptionOptions.Default.retryRandomizationFactor, actual.retryRandomizationFactor)
        assertNotEquals(SubscriptionOptions.Default.retryRandomizer, actual.retryRandomizer)
    }
}
