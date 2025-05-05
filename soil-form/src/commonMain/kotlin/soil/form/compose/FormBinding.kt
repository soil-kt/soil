package soil.form.compose

import androidx.compose.runtime.Stable
import soil.form.FieldName
import soil.form.FieldNames
import soil.form.FormPolicy
import soil.form.FormRule
import soil.form.FormRules
import soil.form.annotation.InternalSoilFormApi

@InternalSoilFormApi
@Stable
interface FormBinding<T : Any> {
    val value: T
    val policy: FormPolicy
    val rules: FormRules<T>

    operator fun get(name: FieldName): FieldMetaState?
    operator fun set(name: FieldName, fieldMeta: FieldMetaState)

    fun register(name: FieldName, dependsOn: FieldNames, rule: FormRule<T>)
    fun unregister(name: FieldName)
    fun revalidateDependents(name: FieldName)
    fun handleChange(updater: T.() -> T)
    fun handleSubmit()
}

@Stable
interface HasFormBinding<T : Any> {
    @InternalSoilFormApi
    val binding: FormBinding<T>
}
