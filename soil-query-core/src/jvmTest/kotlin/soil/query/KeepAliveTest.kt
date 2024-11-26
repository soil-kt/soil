// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.yield
import soil.testing.UnitTest
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

// Some of the methods required for testing are only implemented in the coroutine-jvm,
// so we implement them in a jvmTest folder.
class KeepAliveTest : UnitTest() {

    @OptIn(ExperimentalCoroutinesApi::class, DelicateCoroutinesApi::class)
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @OptIn(ExperimentalCoroutinesApi::class)
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @Test
    fun testZero() = testRun(
        keepAliveTime = Duration.ZERO,
        callerDelay = 300.milliseconds
    )

    @Test
    fun testShort() = testRun(
        keepAliveTime = 300.milliseconds,
        callerDelay = 300.milliseconds
    )

    @Test
    fun testLong() = testRun(
        keepAliveTime = 1.seconds,
        callerDelay = 300.milliseconds
    )

    private fun testRun(
        keepAliveTime: Duration,
        callerDelay: Duration,
        times: Int = 30
    ) {
        runBlocking {
            val swrScope = SwrCacheScope()
            val swrClient = SwrCache(
                policy = SwrCachePolicy(
                    coroutineScope = swrScope,
                    queryOptions = QueryOptions(
                        keepAliveTime = keepAliveTime,
                        logger = { println(it) }
                    )
                )
            )
            repeat(times) {
                val scope = CoroutineScope(Dispatchers.Main + Job())
                swrClient.getQuery(GetTestQueryKey()).use { query ->
                    yield()
                    scope.launch {
                        query.resume()
                    }.join()
                    scope.cancel()
                    delay(callerDelay)
                }
            }
        }
    }

    class GetTestQueryKey : QueryKey<String> by buildQueryKey(
        id = Id,
        fetch = {
            UUID.randomUUID().toString()
        }
    ) {
        object Id : QueryId<String>(
            namespace = "test/query"
        )
    }
}
