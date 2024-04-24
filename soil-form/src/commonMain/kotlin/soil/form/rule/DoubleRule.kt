// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.fieldError
import soil.form.FieldErrors
import soil.form.ValidationRule
import soil.form.ValidationRuleBuilder
import soil.form.noErrors

typealias DoubleRule = ValidationRule<Double>
typealias DoubleRuleBuilder = ValidationRuleBuilder<Double>

class DoubleRuleTester(
    val predicate: Double.() -> Boolean,
    val message: () -> String
) : DoubleRule {
    override fun test(value: Double): FieldErrors {
        return if (value.predicate()) noErrors else fieldError(message())
    }
}

fun DoubleRuleBuilder.minimum(limit: Double, message: () -> String) {
    extend(DoubleRuleTester({ this >= limit }, message))
}

fun DoubleRuleBuilder.maximum(limit: Double, message: () -> String) {
    extend(DoubleRuleTester({ this <= limit }, message))
}

fun DoubleRuleBuilder.isNaN(message: () -> String) {
    extend(DoubleRuleTester({ isNaN() }, message))
}

fun DoubleRuleBuilder.notNaN(message: () -> String) {
    extend(DoubleRuleTester({ !isNaN() }, message))
}
