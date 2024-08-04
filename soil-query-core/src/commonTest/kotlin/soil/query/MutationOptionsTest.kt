// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.testing.UnitTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.time.Duration.Companion.seconds

class MutationOptionsTest : UnitTest() {

    @Test
    fun factory_default() {
        val actual = MutationOptions()
        assertEquals(MutationOptions.Default.isOneShot, actual.isOneShot)
        assertEquals(MutationOptions.Default.isStrictMode, actual.isStrictMode)
        assertEquals(MutationOptions.Default.onError, actual.onError)
        assertEquals(MutationOptions.Default.shouldExecuteEffectSynchronously, actual.shouldExecuteEffectSynchronously)
        assertEquals(MutationOptions.Default.keepAliveTime, actual.keepAliveTime)
        assertEquals(MutationOptions.Default.logger, actual.logger)
        assertEquals(MutationOptions.Default.shouldRetry, actual.shouldRetry)
        assertEquals(MutationOptions.Default.retryCount, actual.retryCount)
        assertEquals(MutationOptions.Default.retryInitialInterval, actual.retryInitialInterval)
        assertEquals(MutationOptions.Default.retryMaxInterval, actual.retryMaxInterval)
        assertEquals(MutationOptions.Default.retryMultiplier, actual.retryMultiplier)
        assertEquals(MutationOptions.Default.retryRandomizationFactor, actual.retryRandomizationFactor)
        assertEquals(MutationOptions.Default.retryRandomizer, actual.retryRandomizer)
    }

    @Test
    fun factory_specifyingArguments() {
        val actual = MutationOptions(
            isOneShot = true,
            isStrictMode = true,
            onError = { },
            shouldExecuteEffectSynchronously = true,
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
        assertNotEquals(MutationOptions.Default.isOneShot, actual.isOneShot)
        assertNotEquals(MutationOptions.Default.isStrictMode, actual.isStrictMode)
        assertNotEquals(MutationOptions.Default.onError, actual.onError)
        assertNotEquals(MutationOptions.Default.shouldExecuteEffectSynchronously, actual.shouldExecuteEffectSynchronously)
        assertNotEquals(MutationOptions.Default.keepAliveTime, actual.keepAliveTime)
        assertNotEquals(MutationOptions.Default.logger, actual.logger)
        assertNotEquals(MutationOptions.Default.shouldRetry, actual.shouldRetry)
        assertNotEquals(MutationOptions.Default.retryCount, actual.retryCount)
        assertNotEquals(MutationOptions.Default.retryInitialInterval, actual.retryInitialInterval)
        assertNotEquals(MutationOptions.Default.retryMaxInterval, actual.retryMaxInterval)
        assertNotEquals(MutationOptions.Default.retryMultiplier, actual.retryMultiplier)
        assertNotEquals(MutationOptions.Default.retryRandomizationFactor, actual.retryRandomizationFactor)
        assertNotEquals(MutationOptions.Default.retryRandomizer, actual.retryRandomizer)
    }

    @Test
    fun copy_default() {
        val actual = MutationOptions.Default.copy()
        assertEquals(MutationOptions.Default.isOneShot, actual.isOneShot)
        assertEquals(MutationOptions.Default.isStrictMode, actual.isStrictMode)
        assertEquals(MutationOptions.Default.onError, actual.onError)
        assertEquals(MutationOptions.Default.shouldExecuteEffectSynchronously, actual.shouldExecuteEffectSynchronously)
        assertEquals(MutationOptions.Default.keepAliveTime, actual.keepAliveTime)
        assertEquals(MutationOptions.Default.logger, actual.logger)
        assertEquals(MutationOptions.Default.shouldRetry, actual.shouldRetry)
        assertEquals(MutationOptions.Default.retryCount, actual.retryCount)
        assertEquals(MutationOptions.Default.retryInitialInterval, actual.retryInitialInterval)
        assertEquals(MutationOptions.Default.retryMaxInterval, actual.retryMaxInterval)
        assertEquals(MutationOptions.Default.retryMultiplier, actual.retryMultiplier)
        assertEquals(MutationOptions.Default.retryRandomizationFactor, actual.retryRandomizationFactor)
        assertEquals(MutationOptions.Default.retryRandomizer, actual.retryRandomizer)
    }

    @Test
    fun copy_override() {
        val actual = MutationOptions.Default.copy(
            isOneShot = true,
            isStrictMode = true,
            onError = { },
            shouldExecuteEffectSynchronously = true,
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
        assertNotEquals(MutationOptions.Default.isOneShot, actual.isOneShot)
        assertNotEquals(MutationOptions.Default.isStrictMode, actual.isStrictMode)
        assertNotEquals(MutationOptions.Default.onError, actual.onError)
        assertNotEquals(MutationOptions.Default.shouldExecuteEffectSynchronously, actual.shouldExecuteEffectSynchronously)
        assertNotEquals(MutationOptions.Default.keepAliveTime, actual.keepAliveTime)
        assertNotEquals(MutationOptions.Default.logger, actual.logger)
        assertNotEquals(MutationOptions.Default.shouldRetry, actual.shouldRetry)
        assertNotEquals(MutationOptions.Default.retryCount, actual.retryCount)
        assertNotEquals(MutationOptions.Default.retryInitialInterval, actual.retryInitialInterval)
        assertNotEquals(MutationOptions.Default.retryMaxInterval, actual.retryMaxInterval)
        assertNotEquals(MutationOptions.Default.retryMultiplier, actual.retryMultiplier)
        assertNotEquals(MutationOptions.Default.retryRandomizationFactor, actual.retryRandomizationFactor)
        assertNotEquals(MutationOptions.Default.retryRandomizer, actual.retryRandomizer)
    }
}
