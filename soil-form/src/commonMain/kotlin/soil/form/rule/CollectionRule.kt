// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.FieldErrors
import soil.form.ValidationRule
import soil.form.ValidationRuleBuilder
import soil.form.fieldError
import soil.form.noErrors

// TODO: ListRule, SetRule, MapRule

typealias CollectionRule<V> = ValidationRule<Collection<V>>
typealias CollectionRuleBuilder<V> = ValidationRuleBuilder<Collection<V>>

/**
 * A rule that tests the collection value.
 *
 * @property predicate The predicate to test the collection value. Returns `true` if the test passes; `false` otherwise.
 * @property message The message to return when the test fails.
 * @constructor Creates a new instance of [CollectionRuleTester].
 */
class CollectionRuleTester<V>(
    val predicate: Collection<V>.() -> Boolean,
    val message: () -> String
) : CollectionRule<V> {
    override fun test(value: Collection<V>): FieldErrors {
        return if (value.predicate()) noErrors else fieldError(message())
    }
}

/**
 * Validates that the collection is not empty.
 *
 * Usage:
 * ```kotlin
 * rules<Collection<String>> {
 *     notEmpty { "must not be empty" }
 * }
 * ```
 *
 * @param message The message to return when the test fails.
 */
fun <V> CollectionRuleBuilder<V>.notEmpty(message: () -> String) {
    extend(CollectionRuleTester(Collection<V>::isNotEmpty, message))
}

/**
 * Validates that the collection size is at least [limit].
 *
 * Usage:
 * ```kotlin
 * rules<Collection<String>> {
 *     minSize(3) { "must have at least 3 items" }
 * }
 * ```
 *
 * @param limit The minimum number of elements the collection must have.
 * @param message The message to return when the test fails.
 */
fun <V> CollectionRuleBuilder<V>.minSize(limit: Int, message: () -> String) {
    extend(CollectionRuleTester({ size >= limit }, message))
}

/**
 * Validates that the collection size is no more than [limit].
 *
 * Usage:
 * ```kotlin
 * rules<Collection<String>> {
 *     maxSize(20) { "must have at no more 20 items" }
 * }
 * ```
 *
 * @param limit The maximum number of elements the collection can have.
 * @param message The message to return when the test fails.
 */
fun <V> CollectionRuleBuilder<V>.maxSize(limit: Int, message: () -> String) {
    extend(CollectionRuleTester({ size <= limit }, message))
}
