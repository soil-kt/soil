// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

fun interface ValidationRule<V> {
    fun test(value: V): FieldErrors
}

typealias ValidationRuleSet<V> = Set<ValidationRule<V>>
