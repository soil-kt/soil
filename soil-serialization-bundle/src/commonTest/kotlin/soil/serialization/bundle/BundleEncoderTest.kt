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
class BundleEncoderTest : UnitTest() {
    @Test
    fun encodeToBundle_primitiveTypes() {
        assertEquals(true, Bundler.encodeToBundle(true).getBoolean(null))
        assertEquals(8.toByte(), Bundler.encodeToBundle(8.toByte()).getByte(null))
        assertEquals('a', Bundler.encodeToBundle('a').getChar(null))
        assertEquals(3.14, Bundler.encodeToBundle(3.14).getDouble(null))
        assertEquals(3.14f, Bundler.encodeToBundle(3.14f).getFloat(null))
        assertEquals(30, Bundler.encodeToBundle(30).getInt(null))
        assertEquals(30L, Bundler.encodeToBundle(30L).getLong(null))
        assertEquals(30.toShort(), Bundler.encodeToBundle(30.toShort()).getShort(null))
        assertEquals("hello", Bundler.encodeToBundle("hello").getString(null))
        assertEquals(EnumTestData.Bar.ordinal, Bundler.encodeToBundle(EnumTestData.Bar).getInt(null))
    }

    @Test
    fun encodeToBundle_collectionList() {
        val list = listOf(30, 20, 10)
        val actual = Bundler.encodeToBundle(list)
        assertEquals(3, actual.getInt(Bundler.COLLECTION_SIZE_KEY))
        assertEquals(30, actual.getInt("0"))
        assertEquals(20, actual.getInt("1"))
        assertEquals(10, actual.getInt("2"))
    }

    @Test
    fun encodeToBundle_collectionMap() {
        val map = mapOf("k1" to 30, "k2" to 20)
        val actual = Bundler.encodeToBundle(map)
        assertEquals(2, actual.getInt(Bundler.COLLECTION_SIZE_KEY))
        assertEquals("k1", actual.getString("0"))
        assertEquals(30, actual.getInt("1"))
        assertEquals("k2", actual.getString("2"))
        assertEquals(20, actual.getInt("3"))
    }

    @Test
    fun encodeToBundle_classType() {
        val obj = ObjectTestData("John", 30)
        val actual = Bundler.encodeToBundle(obj)
        assertEquals("John", actual.getString("name"))
        assertEquals(30, actual.getInt("age"))
    }

    @Test
    fun encodeToBundle_optionalType() {
        val obj = ObjectTestData(null, 30)
        val actual = Bundler.encodeToBundle(obj)
        assertEquals(false, actual.containsKey("name"))
        assertEquals(30, actual.getInt("age"))
    }

    @Test
    fun encodeToBundle_sealedType() {
        val obj = SealedTestData.Bar(30)
        val actual = Bundler.encodeToBundle<SealedTestData>(obj)
        assertEquals(true, actual.containsKey("type"))
        assertEquals(true, actual.containsKey("value"))
        val nested = actual.getBundle("value")!!
        assertEquals(30, nested.getInt("age"))
    }

    @Test
    fun encodeToBundle_polymorphicType() {
        val obj = PolymorphicBarData(30)
        val bundler = Bundler(serializersModule = SerializersModule {
            polymorphic(PolymorphicTestData::class) {
                subclass(PolymorphicFooData::class)
                subclass(PolymorphicBarData::class)
            }
        })
        val polymorphicSerializer = PolymorphicSerializer(PolymorphicTestData::class)
        val actual = bundler.encodeToBundle(obj, polymorphicSerializer)
        assertEquals(true, actual.containsKey("type"))
        assertEquals(true, actual.containsKey("value"))
        val nested = actual.getBundle("value")!!
        assertEquals(30, nested.getInt("age"))
    }

    @Test
    fun encodeToBundle_testData1() {
        val data = TestData1(ObjectTestData("John", 30), "Tokyo")
        val actual = Bundler.encodeToBundle(data)
        assertEquals(true, actual.containsKey("data"))
        val nested = actual.getBundle("data")!!
        assertEquals("John", nested.getString("name"))
        assertEquals(30, nested.getInt("age"))
        assertEquals("Tokyo", actual.getString("address"))
    }

    @Test
    fun encodeToBundle_testData2() {
        val data = TestData2(EnumTestData.Bar, "Tokyo")
        val actual = Bundler.encodeToBundle(data)
        assertEquals(EnumTestData.Bar.ordinal, actual.getInt("data"))
        assertEquals("Tokyo", actual.getString("address"))
    }

    @Test
    fun encodeToBundle_testData3() {
        val data = TestData3(listOf(ObjectTestData("John", 30), ObjectTestData("Jane", 20)), "Tokyo")
        val actual = Bundler.encodeToBundle(data)
        assertEquals(true, actual.containsKey("data"))
        val nested = actual.getBundle("data")!!
        assertEquals(2, nested.getInt(Bundler.COLLECTION_SIZE_KEY))
        assertEquals(true, nested.containsKey("0"))
        val nestedData1 = nested.getBundle("0")!!
        assertEquals("John", nestedData1.getString("name"))
        assertEquals(30, nestedData1.getInt("age"))
        assertEquals(true, nested.containsKey("1"))
        val nestedData2 = nested.getBundle("1")!!
        assertEquals("Jane", nestedData2.getString("name"))
        assertEquals(20, nestedData2.getInt("age"))
        assertEquals("Tokyo", actual.getString("address"))
    }

    @Test
    fun encodeToBundle_testData4() {
        val data = TestData4(SealedTestData.Bar(30), "Tokyo")
        val actual = Bundler.encodeToBundle(data)
        assertEquals(true, actual.containsKey("data"))
        val nested = actual.getBundle("data")!!
        assertEquals(true, nested.containsKey("type"))
        assertEquals(true, nested.containsKey("value"))
        val nestedValue = nested.getBundle("value")!!
        assertEquals(30, nestedValue.getInt("age"))
        assertEquals("Tokyo", actual.getString("address"))
    }

    @Test
    fun encodeToBundle_testData5() {
        val data = TestData5(PolymorphicBarData(30), "Tokyo")
        val bundler = Bundler(serializersModule = SerializersModule {
            polymorphic(PolymorphicTestData::class) {
                subclass(PolymorphicFooData::class)
                subclass(PolymorphicBarData::class)
            }
        })
        val actual = bundler.encodeToBundle(data)
        assertEquals(true, actual.containsKey("data"))
        val nested = actual.getBundle("data")!!
        assertEquals(true, nested.containsKey("type"))
        assertEquals(true, nested.containsKey("value"))
        val nestedValue = nested.getBundle("value")!!
        assertEquals(30, nestedValue.getInt("age"))
        assertEquals("Tokyo", actual.getString("address"))
    }

    @Test
    fun encodeToBundle_testData6() {
        val data = TestData6(ContextualTestData(2024, 6, 23), "Tokyo")
        val bundler = Bundler(serializersModule = SerializersModule {
            contextual(ContextualTestData::class, ContextualTestDataAsStringSerializer)
        })
        val actual = bundler.encodeToBundle(data)
        assertEquals("2024-6-23", actual.getString("data"))
        assertEquals("Tokyo", actual.getString("address"))
    }
}
