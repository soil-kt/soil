// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.fieldError
import soil.form.FieldErrors
import soil.form.ValidationRule
import soil.form.ValidationRuleBuilder
import soil.form.noErrors

typealias FloatRule = ValidationRule<Float>
typealias FloatRuleBuilder = ValidationRuleBuilder<Float>

class FloatRuleTester(
    val predicate: Float.() -> Boolean,
    val message: () -> String
) : FloatRule {
    override fun test(value: Float): FieldErrors {
        return if (value.predicate()) noErrors else fieldError(message())
    }
}

fun FloatRuleBuilder.minimum(limit: Float, message: () -> String) {
    extend(FloatRuleTester({ this >= limit }, message))
}

fun FloatRuleBuilder.maximum(limit: Float, message: () -> String) {
    extend(FloatRuleTester({ this <= limit }, message))
}

fun FloatRuleBuilder.isNaN(message: () -> String) {
    extend(FloatRuleTester({ isNaN() }, message))
}

fun FloatRuleBuilder.notNaN(message: () -> String) {
    extend(FloatRuleTester({ !isNaN() }, message))
}
