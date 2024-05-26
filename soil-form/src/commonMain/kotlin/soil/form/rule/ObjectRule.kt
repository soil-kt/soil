// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.FieldErrors
import soil.form.ValidationRule
import soil.form.ValidationRuleBuilder
import soil.form.fieldError
import soil.form.noErrors
import soil.form.rules

typealias ObjectRule<V> = ValidationRule<V>
typealias ObjectRuleBuilder<V> = ValidationRuleBuilder<V>

/**
 * A rule that tests the object value.
 *
 * @property predicate The predicate to test the object value. Returns `true` if the test passes; `false` otherwise.
 * @property message The message to return when the test fails.
 * @constructor Creates a new instance of [ObjectRuleTester].
 */
class ObjectRuleTester<V>(
    val predicate: V.() -> Boolean,
    val message: () -> String
) : ObjectRule<V> {
    override fun test(value: V): FieldErrors {
        return if (value.predicate()) noErrors else fieldError(message())
    }
}

/**
 * A rule that chains a transformation function with a set of rules.
 *
 * @property transform The transformation function to apply to the object value.
 * @constructor Creates a new instance of [ObjectRuleChainer].
 */
class ObjectRuleChainer<V, S>(
    val transform: (V) -> S
) : ObjectRule<V> {

    private var ruleSet: Set<ValidationRule<S>> = emptySet()

    override fun test(value: V): FieldErrors {
        return transform(value).let { ruleSet.flatMap { rule -> rule.test(it) } }
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
    extend(ObjectRuleTester(predicate, message))
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
    return ObjectRuleChainer(transform).also { extend(it) }
}
