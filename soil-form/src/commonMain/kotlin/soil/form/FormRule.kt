// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

fun interface FormRule<T> {
    fun test(value: T, dryRun: Boolean): Boolean
}
