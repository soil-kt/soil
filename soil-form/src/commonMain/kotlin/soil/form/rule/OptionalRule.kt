// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.fieldError
import soil.form.FieldErrors
import soil.form.ValidationRule
import soil.form.ValidationRuleBuilder
import soil.form.rules

typealias OptionalRule<V> = ValidationRule<V?>
typealias OptionalRuleBuilder<V> = ValidationRuleBuilder<V?>

class OptionalRuleChainer<V>(
    val message: () -> String
) : OptionalRule<V> {

    private var ruleSet: Set<ValidationRule<V>> = emptySet()

    override fun test(value: V?): FieldErrors {
        return if (value != null) {
            ruleSet.flatMap { rule -> rule.test(value) }
        } else {
            fieldError(message())
        }
    }

    infix fun then(block: ValidationRuleBuilder<V>.() -> Unit) {
        ruleSet = rules(block)
    }
}

fun <V : Any> OptionalRuleBuilder<V?>.notNull(message: () -> String): OptionalRuleChainer<V> {
    return OptionalRuleChainer<V>(message).also { extend(it) }
}
