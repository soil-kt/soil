// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.FieldErrors
import soil.form.ValidationRule
import soil.form.ValidationRuleBuilder
import soil.form.fieldError
import soil.form.noErrors

typealias ArrayRule<V> = ValidationRule<Array<V>>
typealias ArrayRuleBuilder<V> = ValidationRuleBuilder<Array<V>>

/**
 * A rule that tests the array value.
 *
 * @property predicate The predicate to test the array value. Returns `true` if the test passes; `false` otherwise.
 * @property message The message to return when the test fails.
 * @constructor Creates a new instance of [ArrayRuleTester].
 */
class ArrayRuleTester<V>(
    val predicate: Array<V>.() -> Boolean,
    val message: () -> String
) : ArrayRule<V> {
    override fun test(value: Array<V>): FieldErrors {
        return if (value.predicate()) noErrors else fieldError(message())
    }
}

/**
 * Validates that the array is not empty.
 *
 * Usage:
 * ```kotlin
 * rules<Array<String>> {
 *     notEmpty { "must be not empty" }
 * }
 * ```
 *
 * @param message The message to return when the test fails.
 */
fun <V> ArrayRuleBuilder<V>.notEmpty(message: () -> String) {
    extend(ArrayRuleTester(Array<V>::isNotEmpty, message))
}

/**
 * Validates that the array size is at least [limit].
 *
 * Usage:
 * ```kotlin
 * rules<Array<String>> {
 *     minSize(3) { "must have at least 3 items" }
 * }
 * ```
 *
 * @param limit The minimum number of elements the array must have.
 * @param message The message to return when the test fails.
 */
fun <V> ArrayRuleBuilder<V>.minSize(limit: Int, message: () -> String) {
    extend(ArrayRuleTester({ size >= limit }, message))
}

/**
 * Validates that the array size is no more than [limit].
 *
 * Usage:
 * ```kotlin
 * rules<Array<String>> {
 *     maxSize(20) { "must have at no more 20 items" }
 * }
 * ```
 *
 * @param limit The maximum number of elements the array can have.
 * @param message The message to return when the test fails.
 */
fun <V> ArrayRuleBuilder<V>.maxSize(limit: Int, message: () -> String) {
    extend(ArrayRuleTester({ size <= limit }, message))
}
