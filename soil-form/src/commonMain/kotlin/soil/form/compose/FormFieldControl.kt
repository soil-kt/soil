package soil.form.compose

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import soil.form.FieldErrors
import soil.form.FieldName
import soil.form.FieldNames
import soil.form.FieldPolicy
import soil.form.FieldValidateOn
import soil.form.ValidationRuleSet
import soil.form.annotation.InternalSoilFormApi

@Stable
interface FormFieldControl<V> {
    val name: FieldName
    val value: V
    val errors: FieldErrors
    val isDirty: Boolean
    val isTouched: Boolean
    val isFocused: Boolean
    val isDisabled: Boolean

    fun onValueChange(value: V)
    fun onFocus()
    fun onBlur()
    fun trigger(validateOn: FieldValidateOn)
}

inline val FormFieldControl<*>.hasError
    get() = errors.isNotEmpty()

@InternalSoilFormApi
internal class FormFieldController<T : Any, V>(
    private val form: FormBinding<T>,
    private val selector: (T) -> V,
    private val updater: T.(V) -> T,
    private val rules: ValidationRuleSet<V>,
    override val name: FieldName,
    private val dependsOn: FieldNames,
) : FormFieldControl<V> {

    val policy: FieldPolicy = form.policy.field

    private val meta: FieldMetaState = form[name] ?: fieldMetaStateOf(
        trigger = policy.validationTrigger.startAt
    )

    override val value: V by derivedStateOf {
        selector(form.value)
    }

    override var errors: FieldErrors
        get() = meta.errors
        set(value) {
            meta.errors = value
        }

    override var isDirty: Boolean
        get() = meta.isDirty
        set(value) {
            meta.isDirty = value
        }

    override var isTouched: Boolean
        get() = meta.isTouched
        set(value) {
            meta.isTouched = value
        }

    override var isFocused: Boolean by mutableStateOf(false)

    override var isDisabled: Boolean by mutableStateOf(false)

    fun register() {
        form.register(name, dependsOn) { value, dryRun ->
            validate(selector(value), dryRun)
        }
        if (form[name] == null) {
            form[name] = meta
        }
    }

    fun unregister() {
        form.unregister(name)
    }

    override fun onValueChange(value: V) {
        form.handleChange { updater(value) }
        isDirty = true
    }

    override fun onFocus() {
        isFocused = true
    }

    override fun onBlur() {
        isTouched = isTouched || isFocused
        isFocused = false
    }

    override fun trigger(validateOn: FieldValidateOn) {
        if (shouldTrigger(validateOn)) {
            validate(value)
        }
    }

    fun shouldTrigger(validateOn: FieldValidateOn): Boolean {
        return validateOn == meta.trigger
    }

    private fun validate(value: V, dryRun: Boolean = false): Boolean {
        val error = rules.flatMap { it.test(value) }
        val isPassed = error.isEmpty()
        if (!dryRun) {
            meta.errors = error
            meta.trigger = policy.validationTrigger.next(meta.trigger, isPassed)
            meta.hasBeenValidated = true
        }
        return isPassed
    }

    fun revalidateDependents() {
        form.revalidateDependents(name)
    }
}
