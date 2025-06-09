// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.core

import soil.form.annotation.SoilFormDsl

/**
 * A builder class for creating sets of validation rules using a DSL approach.
 *
 * This builder provides a convenient way to compose multiple validation rules together
 * using a fluent API. It's typically used within validation rule DSL blocks to build
 * complex validation logic from simpler rule components.
 *
 * Note: Instead of manually creating validation rules with lambda functions, it's recommended
 * to use the type-specific rule extensions available in the `soil.form.rule` package,
 * which provide convenient DSL methods for common validation scenarios.
 *
 * Usage:
 * ```kotlin
 * val stringRules = rules<String> {
 *     notBlank { "Value is required" }
 *     minLength(3) { "Must be at least 3 characters" }
 *     maxLength(100) { "Must be at most 100 characters" }
 * }
 * ```
 *
 * @param V The type of the value to be validated.
 */
@SoilFormDsl
class ValidationRuleBuilder<V> {
    private val rules: MutableSet<ValidationRule<V>> = mutableSetOf()

    /**
     * Adds a validation rule to the set of rules being built.
     *
     * This method allows you to extend the current rule set with additional validation logic.
     * Rules are evaluated in the order they are added, and all failing rules contribute
     * to the final validation error.
     *
     * @param rule The validation rule to add to the set.
     * @return This builder instance for method chaining.
     */
    fun extend(rule: ValidationRule<V>) = this.apply {
        rules.add(rule)
    }

    /**
     * Adds multiple validation rules to the set of rules being built.
     *
     * This method allows you to extend the current rule set with a collection of
     * validation rules all at once. This is particularly useful when you want to
     * compose rule sets from multiple sources or reuse existing validation rule
     * collections.
     *
     * Rules from the collection are evaluated in their iteration order, and all
     * failing rules contribute to the final validation error.
     *
     * @param rules The collection of validation rules to add to the set.
     * @return This builder instance for method chaining.
     */
    fun extend(rules: Collection<ValidationRule<V>>) = this.apply {
        this.rules.addAll(rules)
    }

    /**
     * Builds and returns the final set of validation rules.
     *
     * This method finalizes the rule building process and returns an immutable set
     * of all the validation rules that have been added to this builder.
     *
     * @return An immutable set containing all the validation rules.
     * @throws IllegalStateException if no rules have been added to the builder.
     */
    fun build(): ValidationRuleSet<V> {
        if (rules.isEmpty()) error("Rule must be at least one.")
        return rules.toSet()
    }
}

/**
 * Creates a set of validation rules using a DSL builder.
 *
 * This function provides a convenient way to create validation rule sets using a
 * domain-specific language (DSL) approach. The builder block allows you to compose
 * multiple validation rules together in a readable and maintainable way.
 *
 * Usage:
 * ```kotlin
 * val stringRules = rules<String> {
 *     notEmpty { "must be not empty" }
 *     maxLength(100) { "must be at most 100 characters" }
 * }
 * ```
 *
 * @param V The type of the value to be validated.
 * @param block A lambda that configures the [ValidationRuleBuilder] to create the rule set.
 * @return A [ValidationRuleSet] containing all the rules defined in the block.
 */
fun <V> rules(block: ValidationRuleBuilder<V>.() -> Unit): ValidationRuleSet<V> {
    return ValidationRuleBuilder<V>().apply(block).build()
}
