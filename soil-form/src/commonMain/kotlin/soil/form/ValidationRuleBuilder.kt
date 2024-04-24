// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

class ValidationRuleBuilder<V> {
    private val rules: MutableSet<ValidationRule<V>> = mutableSetOf()

    fun extend(rule: ValidationRule<V>) = this.apply {
        rules.add(rule)
    }

    fun build(): ValidationRuleSet<V> {
        if (rules.isEmpty()) error("Rule must be at least one.")
        return rules.toSet()
    }
}

fun <T> rules(block: ValidationRuleBuilder<T>.() -> Unit): ValidationRuleSet<T> {
    return ValidationRuleBuilder<T>().apply(block).build()
}
