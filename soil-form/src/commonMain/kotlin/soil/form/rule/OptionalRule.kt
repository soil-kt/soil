// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.core.ValidationResult
import soil.form.core.ValidationRule
import soil.form.core.ValidationRuleBuilder
import soil.form.core.rules

typealias OptionalRule<V> = ValidationRule<V?>
typealias OptionalRuleBuilder<V> = ValidationRuleBuilder<V?>

/**
 * A rule that chains non-optional rules. If the value is not `null`, the rule set is applied to the value.
 *
 * @property message The message to return when the value is `null`.
 * @constructor Creates a new instance of [OptionalRuleChainer].
 */
class OptionalRuleChainer<V>(
    val message: () -> String
) : OptionalRule<V> {

    private var ruleSet: Set<ValidationRule<V>> = emptySet()

    override fun test(value: V?): ValidationResult {
        if (value == null) {
            return ValidationResult.Invalid(message())
        }
        val errors = ruleSet.flatMap { rule ->
            when (val result = rule.test(value)) {
                is ValidationResult.Valid -> emptyList()
                is ValidationResult.Invalid -> result.errors
            }
        }
        return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
    }

    /**
     * Chains a set of rules to the non-optional value.
     *
     * Usage:
     * ```kotlin
     * rules<String?> {
     *     notNull { "must be not null" } then {
     *         minLength(3) { "must be at least 3 characters" }
     *     }
     * }
     *
     * @param block The block to build the rule set.
     */
    infix fun then(block: ValidationRuleBuilder<V>.() -> Unit) {
        ruleSet = rules(block)
    }
}

/**
 * Validates that the optional value is not `null`.
 *
 * ```kotlin
 * rules<String?> {
 *     notNull { "must be not null" } then {
 *         minLength(3) { "must be at least 3 characters" }
 *     }
 * }
 *
 * @param message The message to return when the value is `null`.
 * @return The rule chainer to chain the non-optional rules.
 */
fun <V : Any> OptionalRuleBuilder<V?>.notNull(message: () -> String): OptionalRuleChainer<V> {
    return OptionalRuleChainer<V>(message).also { extend(it) }
}
