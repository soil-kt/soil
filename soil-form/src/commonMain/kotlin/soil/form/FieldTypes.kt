// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

/**
 * This field name is used to uniquely identify a field within a form.
 */
typealias FieldName = String
typealias FieldNames = Set<FieldName>

object FieldNameFactory {
    operator fun component1(): FieldName = "field1"
    operator fun component2(): FieldName = "field2"
    operator fun component3(): FieldName = "field3"
    operator fun component4(): FieldName = "field4"
    operator fun component5(): FieldName = "field5"
    operator fun component6(): FieldName = "field6"
    operator fun component7(): FieldName = "field7"
    operator fun component8(): FieldName = "field8"
    operator fun component9(): FieldName = "field9"
    operator fun component10(): FieldName = "field10"
    operator fun component11(): FieldName = "field11"
    operator fun component12(): FieldName = "field12"
    operator fun component13(): FieldName = "field13"
    operator fun component14(): FieldName = "field14"
    operator fun component15(): FieldName = "field15"
    operator fun component16(): FieldName = "field16"
}


/**
 * Represents a single error message in the field.
 */
typealias FieldError = String

/**
 * Represents multiple error messages in the field.
 */
typealias FieldErrors = List<FieldError>

/**
 * Creates error messages for a field.
 *
 * @param messages Error messages. There must be at least one error message.
 * @return The generated error messages for the field.
 */
fun fieldError(vararg messages: String): FieldErrors {
    require(messages.isNotEmpty())
    return listOf(*messages)
}

/**
 * Syntax sugar representing that there are no errors in the field.
 */
val noErrors: FieldErrors = emptyList()
