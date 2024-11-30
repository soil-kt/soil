// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FilterResolverTest : UnitTest() {

    @Test
    fun testForEach_type() {
        val store = mapOf<UniqueId, TestModel>(
            TestId("1") to TestModel.create("1"),
            TestId("2") to TestModel.create(RuntimeException("2")),
            TestId("3") to TestModel.create("3")
        )
        val cache = mapOf<UniqueId, TestModel>(
            TestId("4") to TestModel.create("4"),
            TestId("5") to TestModel.create("5"),
            TestId("6") to TestModel.create("6")
        )
        val resolver = TestFilterResolver(store, cache)
        val filter1 = InvalidateFilter<TestModel>()
        val keys1 = mutableSetOf<UniqueId>()
        resolver.forEach(filter1) { id, _ -> keys1.add(id) }
        assertEquals(keys1.map { it.namespace }.toSet(), setOf("1", "2", "3", "4", "5", "6"))

        val filter2 = InvalidateFilter<TestModel>(
            type = FilterType.Active
        )
        val keys2 = mutableSetOf<UniqueId>()
        resolver.forEach(filter2) { id, _ -> keys2.add(id) }
        assertEquals(keys2.map { it.namespace }.toSet(), setOf("1", "2", "3"))

        val filter3 = InvalidateFilter<TestModel>(
            type = FilterType.Inactive
        )
        val keys3 = mutableSetOf<UniqueId>()
        resolver.forEach(filter3) { id, _ -> keys3.add(id) }
        assertEquals(keys3.map { it.namespace }.toSet(), setOf("4", "5", "6"))
    }

    @Test
    fun testForEach_keys() {
        val store = mapOf<UniqueId, TestModel>(
            TestId("1") to TestModel.create("1"),
            TestId("2") to TestModel.create(RuntimeException("2")),
            TestId("3", "bar") to TestModel.create("3")
        )
        val cache = mapOf<UniqueId, TestModel>(
            TestId("4", "test") to TestModel.create("4"),
            TestId("5") to TestModel.create("5"),
            TestId("6", "foo") to TestModel.create("6")
        )
        val resolver = TestFilterResolver(store, cache)
        val filter1 = InvalidateFilter<TestModel>(
            keys = arrayOf("bar", "foo")
        )
        val keys1 = mutableSetOf<UniqueId>()
        resolver.forEach(filter1) { id, _ -> keys1.add(id) }
        assertEquals(keys1.map { it.namespace }.toSet(), setOf("3", "6"))
    }

    @Test
    fun testForEach_predicate() {
        val store = mapOf<UniqueId, TestModel>(
            TestId("1") to TestModel.create("1"),
            TestId("2") to TestModel.create(RuntimeException("2")),
            TestId("3") to TestModel.create("3")
        )
        val cache = mapOf<UniqueId, TestModel>(
            TestId("4") to TestModel.create("4"),
            TestId("5") to TestModel.create("5"),
            TestId("6") to TestModel.create("6")
        )
        val resolver = TestFilterResolver(store, cache)
        val filter1 = InvalidateFilter<TestModel>(
            predicate = { it.error != null }
        )
        val keys1 = mutableSetOf<UniqueId>()
        resolver.forEach(filter1) { id, _ -> keys1.add(id) }
        assertEquals(keys1.map { it.namespace }.toSet(), setOf("2"))
    }

    @Test
    fun testForEach_combination() {
        val store = mapOf<UniqueId, TestModel>(
            TestId("1") to TestModel.create("1"),
            TestId("2") to TestModel.create(RuntimeException("2")),
            TestId("3", "test") to TestModel.create("3")
        )
        val cache = mapOf<UniqueId, TestModel>(
            TestId("4") to TestModel.create("4"),
            TestId("5") to TestModel.create("5"),
            TestId("6") to TestModel.create("6")
        )
        val resolver = TestFilterResolver(store, cache)
        val filter1 = InvalidateFilter<TestModel>(
            type = FilterType.Active,
            keys = arrayOf("test"),
            predicate = { !it.reply.isNone }
        )
        val keys1 = mutableSetOf<UniqueId>()
        resolver.forEach(filter1) { id, _ -> keys1.add(id) }
        assertEquals(keys1.map { it.namespace }.toSet(), setOf("3"))
    }

    class TestId(
        override val namespace: String,
        override vararg val tags: SurrogateKey = emptyArray()
    ) : UniqueId {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as TestId

            if (namespace != other.namespace) return false
            if (!tags.contentEquals(other.tags)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = namespace.hashCode()
            result = 31 * result + tags.contentHashCode()
            return result
        }
    }

    data class TestModel(
        override val reply: Reply<String>,
        override val replyUpdatedAt: Long,
        override val error: Throwable?,
        override val errorUpdatedAt: Long,
    ) : DataModel<String> {
        override fun isAwaited(): Boolean = false

        companion object {
            fun create(value: String): TestModel {
                return TestModel(Reply.some(value), epoch(), null, 0)
            }

            fun create(error: Throwable): TestModel {
                return TestModel(Reply.none(), epoch(), error, epoch())
            }
        }
    }

    class TestFilterResolver(
        private val store: Map<UniqueId, TestModel>,
        private val cache: Map<UniqueId, TestModel>
    ) : FilterResolver<TestModel> {
        override fun resolveKeys(type: FilterType): Set<UniqueId> = when (type) {
            FilterType.Active -> store.keys
            FilterType.Inactive -> cache.keys
        }

        override fun resolveValue(type: FilterType, id: UniqueId): TestModel? = when (type) {
            FilterType.Active -> store[id]
            FilterType.Inactive -> cache[id]
        }
    }
}
