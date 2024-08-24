// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.serialization.bundle

import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class TestData1(
    val data: ObjectTestData,
    val address: String
)

@Serializable
data class TestData2(
    val data: EnumTestData,
    val address: String
)

@Serializable
data class TestData3(
    val data: List<ObjectTestData>,
    val address: String
)

@Serializable
data class TestData4(
    val data: SealedTestData,
    val address: String
)


@Serializable
data class TestData5(
    @Polymorphic val data: PolymorphicTestData,
    val address: String
)

@Serializable
data class TestData6(
    @Contextual val data: ContextualTestData,
    val address: String
)


@Serializable
data class ObjectTestData(
    val name: String?,
    val age: Int
)

@Serializable
enum class EnumTestData {
    Foo, Bar
}

@Serializable
sealed class SealedTestData {
    @Serializable
    data class Foo(val name: String) : SealedTestData()

    @Serializable
    data class Bar(val age: Int) : SealedTestData()
}

abstract class PolymorphicTestData

@Serializable
data class PolymorphicFooData(val name: String) : PolymorphicTestData()

@Serializable
data class PolymorphicBarData(val age: Int) : PolymorphicTestData()

data class ContextualTestData(
    val year: Int,
    val month: Int,
    val day: Int
)

object ContextualTestDataAsStringSerializer : KSerializer<ContextualTestData> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ContextualTestData", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: ContextualTestData) {
        encoder.encodeString("${value.year}-${value.month}-${value.day}")
    }

    override fun deserialize(decoder: Decoder): ContextualTestData {
        val parts = decoder.decodeString().split("-")
        return ContextualTestData(year = parts[0].toInt(), month = parts[1].toInt(), day = parts[2].toInt())
    }
}
