// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
import soil.form.core.ValidationRule
import soil.form.core.ValidationRuleBuilder
import soil.form.core.rules
import soil.form.core.validate

/**
 * A type alias for validation rules that operate on Collection values.
 *
 * Collection rules are validation functions that take a Collection value and return
 * a [ValidationResult] indicating whether the validation passed or failed.
 */
typealias CollectionRule<V> = ValidationRule<Collection<V>>

/**
 * A type alias for builders that create Collection validation rules.
 *
 * Collection rule builders provide a DSL for constructing validation rules
 * specifically for Collection values, with convenient methods like [notEmpty],
 * [minSize], and [maxSize].
 */
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

@Deprecated("Please migrate to the new form implementation. This legacy code will be removed in a future version.")
class CollectionRuleTester<V>(
    predicate: Collection<V>.() -> Boolean,
    message: () -> String
) : CollectionRule<V> by CollectionRule(predicate, message)

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

/**
 * Creates a validation rule chain for applying rules to each element of the collection.
 *
 * This function allows you to validate each individual element within a collection
 * using the `all` function internally. It's useful when you need to ensure that
 * every element in the collection meets certain criteria.
 *
 * Usage:
 * ```kotlin
 * rules<Collection<String>> {
 *     notEmpty { "collection must not be empty" }
 *     element {
 *         notBlank { "must be not blank" }
 *         minLength(3) { "must be at least 3 characters" }
 *     }
 * }
 * ```
 *
 * @param V The type of the elements in the collection.
 * @param block A lambda that builds the validation rules using [ValidationRuleBuilder].
 */
fun <V> CollectionRuleBuilder<V>.element(block: ValidationRuleBuilder<V>.() -> Unit) {
    val ruleSet = rules(block)
    val chainedRule: CollectionRule<V> = { collection ->
        val allErrorMessages = collection.flatMap { element ->
            when (val result = validate(element, ruleSet)) {
                is ValidationResult.Valid -> emptyList()
                is ValidationResult.Invalid -> result.messages
            }
        }
        if (allErrorMessages.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(allErrorMessages.distinct())
        }
    }
    extend(chainedRule)
}
