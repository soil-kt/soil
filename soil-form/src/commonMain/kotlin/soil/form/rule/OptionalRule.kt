// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
import soil.form.core.ValidationRule
import soil.form.core.ValidationRuleBuilder
import soil.form.core.rules

/**
 * A type alias for validation rules that operate on optional (nullable) values.
 *
 * Optional rules are validation functions that take a nullable value and return
 * a [ValidationResult] indicating whether the validation passed or failed.
 */
typealias OptionalRule<V> = ValidationRule<V?>

/**
 * A type alias for builders that create optional validation rules.
 *
 * Optional rule builders provide a DSL for constructing validation rules
 * specifically for nullable values, with convenient methods like [notNull].
 */
typealias OptionalRuleBuilder<V> = ValidationRuleBuilder<V?>

/**
 * A rule chainer that allows applying non-optional validation rules to optional values.
 *
 * This class enables a fluent API for validating nullable values by first checking
 * for null and then applying a set of validation rules to the non-null value.
 * It provides the `then` infix function to chain additional validation rules.
 *
 * Usage:
 * ```kotlin
 * rules<String?> {
 *     notNull { "Value is required" } then {
 *         notBlank { "Value cannot be blank" }
 *         minLength(3) { "Value must be at least 3 characters" }
 *     }
 * }
 * ```
 *
 * @param V The non-nullable type of the value being validated.
 * @property message The message to return when the value is `null`.
 */
class OptionalRuleChainer<V>(
    val message: () -> String
) {

    private var ruleSet: Set<ValidationRule<V>> = emptySet()

    internal val chainedRule: OptionalRule<V> = { value ->
        if (value == null) {
            ValidationResult.Invalid(message())
        } else {
            val errorMessages = ruleSet.flatMap { rule ->
                when (val result = rule.invoke(value)) {
                    is ValidationResult.Valid -> emptyList()
                    is ValidationResult.Invalid -> result.messages
                }
            }
            if (errorMessages.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errorMessages)
        }
    }

    /**
     * Chains a set of validation rules to be applied to the non-null value.
     *
     * This infix function allows you to specify additional validation rules that
     * will be applied only if the value is not null. If the value is null,
     * only the null check validation will be performed.
     *
     * Usage:
     * ```kotlin
     * rules<String?> {
     *     notNull { "Value is required" } then {
     *         notBlank { "Value cannot be blank" }
     *         minLength(3) { "Value must be at least 3 characters" }
     *         maxLength(50) { "Value must not exceed 50 characters" }
     *     }
     * }
     * ```
     *
     * @param block A lambda that builds the validation rules using [ValidationRuleBuilder].
     */
    infix fun then(block: ValidationRuleBuilder<V>.() -> Unit) {
        ruleSet = rules(block)
    }
}

/**
 * Validates that the optional value is not `null`.
 *
 * This function creates a validation rule that checks if a nullable value is not null.
 * It returns an [OptionalRuleChainer] that allows you to chain additional validation
 * rules using the `then` infix function. The chained rules will only be applied
 * if the value is not null.
 *
 * Usage:
 * ```kotlin
 * rules<String?> {
 *     notNull { "Value is required" } then {
 *         notBlank { "Value cannot be blank" }
 *         minLength(3) { "Value must be at least 3 characters" }
 *     }
 * }
 * ```
 *
 * @param V The non-nullable type of the value being validated.
 * @param message A function that returns the error message when the value is `null`.
 * @return An [OptionalRuleChainer] that allows chaining additional validation rules.
 */
fun <V : Any> OptionalRuleBuilder<V?>.notNull(message: () -> String): OptionalRuleChainer<V> {
    return OptionalRuleChainer<V>(message).also { extend(it.chainedRule) }
}
