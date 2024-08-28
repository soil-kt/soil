// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import soil.testing.UnitTest
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class RetryTest : UnitTest() {

    @Test
    fun testExponentialBackOff() = runTest {
        val options = TestRetryOptions(retryCount = 3)
        val retry = options.exponentialBackOff<Int>()
        val runner = TestBlockRunner(1)
        val result = retry.withRetry { runner.run() }
        assertEquals(2, result)
    }

    @Test
    fun testExponentialBackOff_retryOver() = runTest {
        val options = TestRetryOptions(retryCount = 2)
        val retry = options.exponentialBackOff<Int>()
        val runner = TestBlockRunner(3)
        assertFails {
            retry.withRetry { runner.run() }
        }
    }

    @Test
    fun testExponentialBackOff_retryNone() = runTest {
        val options = TestRetryOptions(retryCount = 1)
        val retry = options.exponentialBackOff<Int>()
        val runner = TestBlockRunner(0)
        val result = retry.withRetry { runner.run() }
        assertEquals(1, result)
    }

    class TestBlockRunner(
        private val errorSimulationCount: Int
    ) {
        private var count = 0
        suspend fun run(): Int {
            delay(1000)
            count++
            return if (count <= errorSimulationCount) {
                throw Exception("Test")
            } else {
                count
            }
        }
    }

    class TestRetryOptions(
        override val shouldRetry: (Throwable) -> Boolean = { true },
        override val retryCount: Int,
        override val retryInitialInterval: Duration = 500.milliseconds,
        override val retryMaxInterval: Duration = 30.seconds,
        override val retryMultiplier: Double = 1.5,
        override val retryRandomizationFactor: Double = 0.5,
        override val retryRandomizer: Random = Random
    ) : RetryOptions
}
