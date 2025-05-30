// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.Stable
import soil.form.FieldName
import soil.form.FieldNames
import soil.form.annotation.InternalSoilFormApi

@InternalSoilFormApi
@Stable
interface FormBinding<T> {
    val value: T
    val policy: FormPolicy

    operator fun get(name: FieldName): FieldMetaState?
    operator fun set(name: FieldName, fieldMeta: FieldMetaState)

    fun register(name: FieldName, dependsOn: FieldNames, rule: FieldRule<T>)
    fun unregister(name: FieldName)
    fun validate(value: T, dryRun: Boolean): Boolean
    fun revalidateDependents(name: FieldName)
    fun handleChange(updater: T.() -> T)
}

fun interface FieldRule<T> {
    fun test(value: T, dryRun: Boolean): Boolean
}

@Stable
interface HasFormBinding<T> {
    @InternalSoilFormApi
    val binding: FormBinding<T>
}
