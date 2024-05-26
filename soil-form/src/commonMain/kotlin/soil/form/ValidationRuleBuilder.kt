// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

/**
 * Builder for creating a set of validation rules.
 *
 * @param V The type of the value to be validated.
 */
class ValidationRuleBuilder<V> {
    private val rules: MutableSet<ValidationRule<V>> = mutableSetOf()

    /**
     * Adds a rule to the set of rules.
     *
     * @param rule The rule to be added.
     */
    fun extend(rule: ValidationRule<V>) = this.apply {
        rules.add(rule)
    }

    /**
     * Builds the set of rules.
     *
     * @return The set of rules.
     */
    fun build(): ValidationRuleSet<V> {
        if (rules.isEmpty()) error("Rule must be at least one.")
        return rules.toSet()
    }
}

/**
 * Creates a set of validation rules.
 *
 * @param V The type of the value to be validated.
 * @param block The block of code to build the rules.
 * @return The set of rules.
 */
fun <V> rules(block: ValidationRuleBuilder<V>.() -> Unit): ValidationRuleSet<V> {
    return ValidationRuleBuilder<V>().apply(block).build()
}
