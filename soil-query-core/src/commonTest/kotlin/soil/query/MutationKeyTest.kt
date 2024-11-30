package soil.query

import kotlinx.coroutines.test.runTest
import soil.query.core.Effect
import soil.query.core.EffectContext
import soil.query.core.getOrNull
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MutationKeyTest : UnitTest() {

    @Test
    fun testMutate() = runTest {
        val testKey = TestMutationKey()
        val testClient = SwrCache(backgroundScope)
        val actual = with(testKey) { testClient.mutationReceiver.mutate(0) }
        assertEquals("Mutate#0", actual)
    }

    @Test
    fun testContentEquals() = runTest {
        val testKey = TestMutationKey()
        assertEquals(testKey.contentEquals("foo", "foo"), true)
        assertEquals(testKey.contentEquals("foo", "bar"), false)
    }

    @Test
    fun testConfigureOptionsOverride() = runTest {
        val testKey = TestMutationKey()
        val testOptions = MutationOptions(isOneShot = false)
        val actual = testKey.onConfigureOptions().invoke(testOptions)
        assertTrue(actual.isOneShot)
    }

    @Test
    fun testMutateEffect() = runTest {
        val testKey = TestMutationKey()
        val testQueryId = QueryId<String>("test")
        val testQueryCache = QueryCacheBuilder {
            put(testQueryId, "initial")
        }
        val testClient = SwrCache(
            policy = SwrCachePolicy(
                coroutineScope = backgroundScope,
                queryCache = testQueryCache
            )
        )
        val testEffectContext = EffectContext(queryEffectClientPropertyKey to testClient)
        val result = with(testKey) { testClient.mutationReceiver.mutate(1) }
        testKey.onMutateEffect(1, result).invoke(testEffectContext)
        val actual = testQueryCache[testQueryId]
        assertEquals("updated:Mutate#1", actual?.reply?.getOrNull())
    }

    class TestMutationKey : MutationKey<String, Int> by buildMutationKey(
        id = MutationId("test"),
        mutate = { variable ->
            "Mutate#$variable"
        }
    ) {

        override val contentEquals: MutationContentEquals<String> = { a, b ->
            a == b
        }

        override fun onConfigureOptions(): MutationOptionsOverride = { options ->
            options.copy(isOneShot = true)
        }

        override fun onMutateEffect(variable: Int, data: String): Effect = {
            queryClient.updateQueryData(QueryId<String>("test")) { "updated:$data" }
        }
    }
}
