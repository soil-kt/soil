package soil.query

import app.cash.turbine.test
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalSoilQueryApi::class)
class SubscriptionKeyTest : UnitTest() {

    @Test
    fun testSubscribe() = runTest {
        val testKey = TestSubscriptionKey()
        val testClient = SwrCachePlus(backgroundScope)
        val testFlow = with(testKey) { testClient.subscriptionReceiver.subscribe() }
        testFlow.test {
            assertEquals("Hello, World!", awaitItem())
            assertEquals("Hello, Soil!", awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun testContentEquals() = runTest {
        val testKey = TestSubscriptionKey()
        assertEquals(testKey.contentEquals("foo", "foo"), true)
        assertEquals(testKey.contentEquals("foo", "bar"), false)
    }

    @Test
    fun testContentCacheable() = runTest {
        val testKey = TestSubscriptionKey()
        assertEquals(testKey.contentCacheable("Hello, World!"), true)
        assertEquals(testKey.contentCacheable(""), false)
    }

    @Test
    fun testConfigureOptionsOverride() = runTest {
        val testKey = TestSubscriptionKey()
        val testOptions = SubscriptionOptions(gcTime = 0.seconds)
        val actual = testKey.onConfigureOptions().invoke(testOptions)
        assertEquals(actual.gcTime, 10.seconds)
    }

    @Test
    fun testInitialData() = runTest {
        val testKey = TestSubscriptionKey()
        val testClient = SwrCachePlus(
            policy = SwrCachePlusPolicy(
                coroutineScope = backgroundScope,
                subscriptionCache = SubscriptionCacheBuilder {
                    put(SubscriptionId("sample"), "Hello, Soil!")
                }
            )
        )
        val actual = testKey.onInitialData().invoke(testClient)
        assertEquals(actual, "Hello, Soil!")
    }

    @Test
    fun testRecoverData() = runTest {
        val testKey = TestSubscriptionKey()
        assertEquals(testKey.onRecoverData().invoke(RuntimeException()), "Recovered")
        val err = assertFails { testKey.onRecoverData().invoke(Exception("test")) }
        assertEquals(err.message, "test")
    }

    class TestSubscriptionKey : SubscriptionKey<String> by buildSubscriptionKey(
        id = SubscriptionId("test"),
        subscribe = {
            flow {
                emit("Hello, World!")
                delay(1000)
                emit("Hello, Soil!")
            }
        }
    ) {

        override val contentEquals: SubscriptionContentEquals<String> = { a, b ->
            a == b
        }

        override val contentCacheable: SubscriptionContentCacheable<String> = {
            it.isNotEmpty()
        }

        override fun onConfigureOptions(): SubscriptionOptionsOverride = { options ->
            options.copy(gcTime = 10.seconds)
        }

        override fun onInitialData(): SubscriptionInitialData<String> = {
            getSubscriptionData(SubscriptionId("sample"))
        }

        override fun onRecoverData(): SubscriptionRecoverData<String> = { err ->
            if (err is RuntimeException) {
                "Recovered"
            } else {
                throw err
            }
        }
    }
}
