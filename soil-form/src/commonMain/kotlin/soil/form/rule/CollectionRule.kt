// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
import soil.form.core.ValidationRule
import soil.form.core.ValidationRuleBuilder

// TODO: ListRule, SetRule, MapRule

typealias CollectionRule<V> = ValidationRule<Collection<V>>
typealias CollectionRuleBuilder<V> = ValidationRuleBuilder<Collection<V>>

/**
 * A rule that tests the collection value.
 *
 * @param predicate The predicate to test the collection value. Returns `true` if the test passes; `false` otherwise.
 * @param message The message to return when the test fails.
 * @return Creates a new instance of [CollectionRule].
 */
fun <V> CollectionRule(
    predicate: Collection<V>.() -> Boolean,
    message: () -> String
): CollectionRule<V> = { value ->
    if (value.predicate()) ValidationResult.Valid else ValidationResult.Invalid(message())
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
    extend(CollectionRule(Collection<V>::isNotEmpty, message))
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
    extend(CollectionRule({ size >= limit }, message))
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
    extend(CollectionRule({ size <= limit }, message))
}
