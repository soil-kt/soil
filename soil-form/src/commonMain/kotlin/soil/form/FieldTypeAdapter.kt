// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form


/**
 * An adapter interface for converting between different type representations of a field value.
 *
 * This interface enables type conversion between:
 * - V: The stored value type in the form data
 * - S: The validation target type (what validators operate on)
 * - U: The input/display type (what the UI components work with)
 *
 * This is useful when you need to validate or display a field value in a different
 * format than how it's stored. For example, storing an integer but displaying it
 * as a string in a text field.
 *
 * Usage:
 * ```kotlin
 * class IntStringAdapter : FieldTypeAdapter<Int, String, String> {
 *     override fun toValidationTarget(value: Int): String = value.toString()
 *     override fun toInput(value: Int): String = value.toString()
 *     override fun fromInput(value: String, current: Int): Int = value.toIntOrNull() ?: current
 * }
 * ```
 *
 * @param V The type of the value as stored in the form data.
 * @param S The type used for validation.
 * @param U The type used for input/display in UI components.
 */
interface FieldTypeAdapter<V, S, U> {

    /**
     * Converts the stored value to the validation target type.
     *
     * @param value The stored value.
     * @return The value converted to the validation target type.
     */
    fun toValidationTarget(value: V): S

    /**
     * Converts the stored value to the input/display type.
     *
     * @param value The stored value.
     * @return The value converted to the input/display type.
     */
    fun toInput(value: V): U

    /**
     * Converts the input/display value back to the stored value type.
     *
     * @param value The input/display value.
     * @param current The current stored value (can be used as fallback).
     * @return The value converted to the stored type.
     */
    fun fromInput(value: U, current: V): V
}

/**
 * A passthrough adapter that performs no type conversion.
 *
 * This adapter is used when the stored value type, validation target type,
 * and input/display type are all the same. It simply returns the value
 * unchanged for all conversion operations.
 *
 * This is the default adapter used when no explicit adapter is provided
 * to a form field.
 *
 * @param V The type used for all representations (stored, validation, and input).
 */
class FieldPassthroughAdapter<V> : FieldTypeAdapter<V, V, V> {
    override fun toValidationTarget(value: V): V = value
    override fun toInput(value: V): V = value
    override fun fromInput(value: V, current: V): V = value
}
