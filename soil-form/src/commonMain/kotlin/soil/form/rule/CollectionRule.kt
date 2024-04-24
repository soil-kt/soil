// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.rule

import soil.form.fieldError
import soil.form.FieldErrors
import soil.form.ValidationRule
import soil.form.ValidationRuleBuilder
import soil.form.noErrors

typealias CollectionRule<V> = ValidationRule<Collection<V>>
typealias CollectionRuleBuilder<V> = ValidationRuleBuilder<Collection<V>>

class CollectionRuleTester<V>(
    val predicate: Collection<V>.() -> Boolean,
    val message: () -> String
) : CollectionRule<V> {
    override fun test(value: Collection<V>): FieldErrors {
        return if (value.predicate()) noErrors else fieldError(message())
    }
}

fun <V> CollectionRuleBuilder<V>.notEmpty(message: () -> String) {
    extend(CollectionRuleTester(Collection<V>::isNotEmpty, message))
}

fun <V> CollectionRuleBuilder<V>.minSize(limit: Int, message: () -> String) {
    extend(CollectionRuleTester({ size >= limit }, message))
}

fun <V> CollectionRuleBuilder<V>.maxSize(limit: Int, message: () -> String) {
    extend(CollectionRuleTester({ size <= limit }, message))
}
