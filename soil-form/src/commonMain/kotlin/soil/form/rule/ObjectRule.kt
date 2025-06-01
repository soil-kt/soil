// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
import soil.form.core.ValidationRule
import soil.form.core.ValidationRuleBuilder
import soil.form.core.rules

/**
 * A type alias for validation rules that operate on any object type.
 *
 * Object rules are validation functions that take a value of any type and return
 * a [ValidationResult] indicating whether the validation passed or failed.
 * This is the most generic form of validation rule.
 */
typealias ObjectRule<V> = ValidationRule<V>

/**
 * A type alias for builders that create object validation rules.
 *
 * Object rule builders provide a DSL for constructing validation rules
 * for any object type, with convenient methods like [test] and [cast].
 */
typealias ObjectRuleBuilder<V> = ValidationRuleBuilder<V>

/**
 * A rule that tests the object value.
 *
 * @param predicate The predicate to test the object value. Returns `true` if the test passes; `false` otherwise.
 * @param message The message to return when the test fails.
 * @return Creates a new instance of [ObjectRule].
 */
fun <V> ObjectRule(
    predicate: V.() -> Boolean,
    message: () -> String
): ObjectRule<V> = { value ->
    if (value.predicate()) ValidationResult.Valid else ValidationResult.Invalid(message())
}

@Deprecated("Please migrate to the new form implementation. This legacy code will be removed in a future version.")
class ObjectRuleTester<V>(
    predicate: V.() -> Boolean,
    message: () -> String
) : ObjectRule<V> by ObjectRule(predicate, message)


/**
 * A rule chainer that allows applying validation rules to transformed object values.
 *
 * This class enables validation of derived or transformed values from the original object.
 * It provides a fluent API for chaining transformation functions with validation rules,
 * allowing complex validation scenarios where you need to validate a computed property
 * or transformed representation of the original value.
 *
 * Usage:
 * ```kotlin
 * rules<String> {
 *     cast { it.length } then {
 *         minimum(3) { "must be at least 3 characters" }
 *         maximum(20) { "must be at most 20 characters" }
 *     }
 * }
 * ```
 *
 * @param V The type of the original object value.
 * @param S The type of the transformed value.
 * @property transform The transformation function to apply to the object value.
 */
class ObjectRuleChainer<V, S>(
    val transform: (V) -> S
) {
    private var ruleSet: Set<ValidationRule<S>> = emptySet()

    internal val chainedRule: ObjectRule<V> = { value ->
        val chainedValue = transform(value)
        val errorMessages = ruleSet.flatMap { rule ->
            when (val result = rule.invoke(chainedValue)) {
                is ValidationResult.Valid -> emptyList()
                is ValidationResult.Invalid -> result.messages
            }
        }
        if (errorMessages.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errorMessages)
    }

    /**
     * Chains a set of validation rules to be applied to the transformed value.
     *
     * This infix function allows you to specify validation rules that will be applied
     * to the result of the transformation function. The original value is first
     * transformed using the transform function, then the chained rules are applied
     * to the transformed value.
     *
     * Usage:
     * ```kotlin
     * rules<String> {
     *     notBlank { "must be not blank" }
     *     cast { it.length } then {
     *         minimum(3) { "must be at least 3 characters" }
     *         maximum(20) { "must be at most 20 characters" }
     *     }
     * }
     * ```
     *
     * @param block A lambda that builds the validation rules using [ValidationRuleBuilder].
     */
    infix fun then(block: ValidationRuleBuilder<S>.() -> Unit) {
        ruleSet = rules(block)
    }

    infix fun then(rules: Set<ValidationRule<S>>) {
        ruleSet = rules
    }
}

/**
 * Validates that the object value passes the given [predicate].
 *
 * Usage:
 * ```kotlin
 * rules<Post> {
 *     test({ title.isNotBlank() }) { "Title must be not blank" }
 * }
 * ```
 *
 * @param predicate The predicate to test the object value. Returns `true` if the test passes; `false` otherwise.
 * @param message The message to return when the test fails.
 */
@Deprecated(
    "Use `satisfy` instead. This will be removed in a future version.",
    ReplaceWith("satisfy(predicate, message)")
)
fun <V : Any> ObjectRuleBuilder<V>.test(predicate: V.() -> Boolean, message: () -> String) {
    extend(ObjectRule(predicate, message))
}

/**
 * Validates that the object value passes the given [predicate].
 *
 * Usage:
 * ```kotlin
 * rules<Post> {
 *     satisfy({ title.isNotBlank() }) { "Title must be not blank" }
 * }
 * ```
 *
 * @param predicate The predicate to test the object value. Returns `true` if the test passes; `false` otherwise.
 * @param message The message to return when the test fails.
 */
fun <V : Any> ObjectRuleBuilder<V>.satisfy(predicate: V.() -> Boolean, message: () -> String) {
    extend(ObjectRule(predicate, message))
}

/**
 * Creates a transformation-based validation rule chain.
 *
 * This function allows you to validate a transformed or derived value from the original object.
 * It's useful when you need to validate a computed property, extracted field, or any
 * transformation of the original value. The transformation is applied first, then the
 * chained validation rules are applied to the transformed result.
 *
 * Usage:
 * ```kotlin
 * rules<String> {
 *     notBlank { "must be not blank" }
 *     cast { it.length } then {
 *         minimum(3) { "must be at least 3 characters" }
 *         maximum(20) { "must be at most 20 characters" }
 *     }
 * }
 * ```
 *
 * @param V The type of the original object value.
 * @param S The type of the transformed value.
 * @param transform The transformation function to apply to the object value.
 * @return An [ObjectRuleChainer] that allows chaining validation rules for the transformed value.
 */
fun <V : Any, S> ObjectRuleBuilder<V>.cast(transform: (V) -> S): ObjectRuleChainer<V, S> {
    return ObjectRuleChainer(transform).also { extend(it.chainedRule) }
}
