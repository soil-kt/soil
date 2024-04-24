// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.fieldError
import soil.form.FieldErrors
import soil.form.ValidationRule
import soil.form.ValidationRuleBuilder
import soil.form.noErrors

typealias ArrayRule<V> = ValidationRule<Array<V>>
typealias ArrayRuleBuilder<V> = ValidationRuleBuilder<Array<V>>

class ArrayRuleTester<V>(
    val predicate: Array<V>.() -> Boolean,
    val message: () -> String
) : ArrayRule<V> {
    override fun test(value: Array<V>): FieldErrors {
        return if (value.predicate()) noErrors else fieldError(message())
    }
}

fun <V> ArrayRuleBuilder<V>.notEmpty(message: () -> String) {
    extend(ArrayRuleTester(Array<V>::isNotEmpty, message))
}

fun <V> ArrayRuleBuilder<V>.minSize(limit: Int, message: () -> String) {
    extend(ArrayRuleTester({ size >= limit }, message))
}

fun <V> ArrayRuleBuilder<V>.maxSize(limit: Int, message: () -> String) {
    extend(ArrayRuleTester({ size <= limit }, message))
}
