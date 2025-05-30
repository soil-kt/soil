// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
import soil.form.core.ValidationRule
import soil.form.core.ValidationRuleBuilder
import soil.form.core.rules

typealias ObjectRule<V> = ValidationRule<V>
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

@Deprecated("Legacy")
class ObjectRuleTester<V>(
    predicate: V.() -> Boolean,
    message: () -> String
) : ObjectRule<V> by ObjectRule(predicate, message)


/**
 * A rule that chains a transformation function with a set of rules.
 *
 * @property transform The transformation function to apply to the object value.
 * @constructor Creates a new instance of [ObjectRuleChainer].
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
     * Chains a set of rules to the transformation function.
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
     * @param block The block to build the set of rules.
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
fun <V : Any> ObjectRuleBuilder<V>.test(predicate: V.() -> Boolean, message: () -> String) {
    extend(ObjectRule(predicate, message))
}

/**
 * Chains a transformation function with a set of rules.
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
 * @param transform The transformation function to apply to the object value.
 */
fun <V : Any, S> ObjectRuleBuilder<V>.cast(transform: (V) -> S): ObjectRuleChainer<V, S> {
    return ObjectRuleChainer(transform).also { extend(it.chainedRule) }
}
