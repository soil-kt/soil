// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.serialization.bundle

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalSerializationApi::class)
class BundleDecoderTest : UnitTest() {
    @Test
    fun decodeFromBundle_primitiveTypes() {
        assertEquals(true, Bundler.decodeFromBundle(Bundler.encodeToBundle(true)))
        assertEquals(8.toByte(), Bundler.decodeFromBundle(Bundler.encodeToBundle(8.toByte())))
        assertEquals('a', Bundler.decodeFromBundle(Bundler.encodeToBundle('a')))
        assertEquals(3.14, Bundler.decodeFromBundle(Bundler.encodeToBundle(3.14)))
        assertEquals(3.14f, Bundler.decodeFromBundle(Bundler.encodeToBundle(3.14f)))
        assertEquals(30, Bundler.decodeFromBundle(Bundler.encodeToBundle(30)))
        assertEquals(30L, Bundler.decodeFromBundle(Bundler.encodeToBundle(30L)))
        assertEquals(30.toShort(), Bundler.decodeFromBundle(Bundler.encodeToBundle(30.toShort())))
        assertEquals("hello", Bundler.decodeFromBundle(Bundler.encodeToBundle("hello")))
        assertEquals(EnumTestData.Bar, Bundler.decodeFromBundle(Bundler.encodeToBundle(EnumTestData.Bar)))
    }

    @Test
    fun decodeFromBundle_collectionList() {
        val expected = listOf(30, 20, 10)
        val actual = Bundler.decodeFromBundle<List<Int>>(Bundler.encodeToBundle(expected))
        assertEquals(expected, actual)
    }

    @Test
    fun decodeFromBundle_collectionMap() {
        val expected = mapOf("k1" to 30, "k2" to 20)
        val actual = Bundler.decodeFromBundle<Map<String, Int>>(Bundler.encodeToBundle(expected))
        assertEquals(expected, actual)
    }

    @Test
    fun decodeFromBundle_classType() {
        val expected = ObjectTestData("John", 30)
        val actual = Bundler.decodeFromBundle<ObjectTestData>(Bundler.encodeToBundle(expected))
        assertEquals(expected.name, actual.name)
        assertEquals(expected.age, actual.age)
    }

    @Test
    fun decodeFromBundle_optionalType() {
        val expected = ObjectTestData(null, 30)
        val actual = Bundler.decodeFromBundle<ObjectTestData>(Bundler.encodeToBundle(expected))
        assertEquals(expected.name, actual.name)
        assertEquals(expected.age, actual.age)
    }

    @Test
    fun decodeFromBundle_sealedType() {
        val expected = SealedTestData.Bar(30)
        val actual = Bundler.decodeFromBundle<SealedTestData>(Bundler.encodeToBundle<SealedTestData>(expected))
        assertEquals(expected, actual)
    }

    @Test
    fun decodeFromBundle_polymorphicType() {
        val expected = PolymorphicBarData(30)
        val bundler = Bundler(serializersModule = SerializersModule {
            polymorphic(PolymorphicTestData::class) {
                subclass(PolymorphicFooData::class)
                subclass(PolymorphicBarData::class)
            }
        })
        val polymorphicSerializer = PolymorphicSerializer(PolymorphicTestData::class)
        val actual = bundler.decodeFromBundle<PolymorphicTestData>(
            bundler.encodeToBundle(expected, polymorphicSerializer),
            polymorphicSerializer
        )
        assertEquals(expected, actual)
    }

    @Test
    fun decodeFromBundle_testData1() {
        val expected = TestData1(ObjectTestData("John", 30), "Tokyo")
        val actual = Bundler.decodeFromBundle<TestData1>(Bundler.encodeToBundle(expected))
        assertEquals(expected.data, actual.data)
        assertEquals(expected.address, actual.address)
    }

    @Test
    fun decodeFromBundle_testData2() {
        val expected = TestData2(EnumTestData.Foo, "Tokyo")
        val actual = Bundler.decodeFromBundle<TestData2>(Bundler.encodeToBundle(expected))
        assertEquals(expected.data, actual.data)
        assertEquals(expected.address, actual.address)
    }

    @Test
    fun decodeFromBundle_testData3() {
        val expected = TestData3(listOf(ObjectTestData("John", 30), ObjectTestData("Jane", 20)), "Tokyo")
        val actual = Bundler.decodeFromBundle<TestData3>(Bundler.encodeToBundle(expected))
        assertEquals(expected.data, actual.data)
        assertEquals(expected.address, actual.address)
    }

    @Test
    fun decodeFromBundle_testData4() {
        val expected = TestData4(SealedTestData.Foo("John"), "Tokyo")
        val actual = Bundler.decodeFromBundle<TestData4>(Bundler.encodeToBundle(expected))
        assertEquals(expected.data, actual.data)
        assertEquals(expected.address, actual.address)
    }

    @Test
    fun decodeFromBundle_testData5() {
        val expected = TestData5(PolymorphicBarData(30), "Tokyo")
        val bundler = Bundler(serializersModule = SerializersModule {
            polymorphic(PolymorphicTestData::class) {
                subclass(PolymorphicFooData::class)
                subclass(PolymorphicBarData::class)
            }
        })
        val actual = bundler.decodeFromBundle<TestData5>(bundler.encodeToBundle(expected))
        assertEquals(expected.data, actual.data)
        assertEquals(expected.address, actual.address)
    }

    @Test
    fun decodeFromBundle_testData6() {
        val expected = TestData6(ContextualTestData(2024, 6, 23), "Tokyo")
        val bundler = Bundler(serializersModule = SerializersModule {
            contextual(ContextualTestData::class, ContextualTestDataAsStringSerializer)
        })
        val actual = bundler.decodeFromBundle<TestData6>(bundler.encodeToBundle(expected))
        assertEquals(expected.data, actual.data)
        assertEquals(expected.address, actual.address)
    }
}
