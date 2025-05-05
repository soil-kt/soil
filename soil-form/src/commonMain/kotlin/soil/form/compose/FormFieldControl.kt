package soil.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import soil.form.FieldErrors
import soil.form.FieldName
import soil.form.FieldNames
import soil.form.FieldPolicy
import soil.form.FieldTypeAdapter
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
    val isEnabled: Boolean

    fun onValueChange(value: V)
    fun onFocus()
    fun onBlur()
    fun handleFocus(hasFocus: Boolean)
    fun trigger(validateOn: FieldValidateOn)
}

inline val FormFieldControl<*>.hasError
    get() = errors.isNotEmpty()


@OptIn(InternalSoilFormApi::class, FlowPreview::class)
@Composable
internal fun <T : Any, V, S, U> Form<T>.rememberFormFieldControl(
    selector: (T) -> V,
    updater: T.(V) -> T,
    adapter: FieldTypeAdapter<V, S, U>,
    name: FieldName,
    dependsOn: FieldNames,
    rules: ValidationRuleSet<S>,
    enabled: Boolean,
): FormFieldControl<U> {
    val control = remember(binding) {
        FormFieldController(
            form = binding,
            selector = selector,
            updater = updater,
            adapter = adapter,
            name = name,
            dependsOn = dependsOn,
            rules = rules
        )
    }.apply { isEnabled = enabled }
    if (enabled) {
        DisposableEffect(control) {
            control.register()
            onDispose {
                control.unregister()
            }
        }
        LaunchedEffect(control) {
            // validateOnMount
            launch {
                snapshotFlow { control.shouldTrigger(FieldValidateOn.Mount) }
                    .filter { it }
                    .debounce(control.fieldPolicy.validationDelay.onMount)
                    .collect {
                        control.trigger(FieldValidateOn.Mount)
                    }
            }

            // validateOnChange
            launch {
                snapshotFlow { adapter.toValidationTarget(control.fieldValue) }
                    .debounce(control.fieldPolicy.validationDelay.onChange)
                    .collect {
                        control.trigger(FieldValidateOn.Change)
                        control.revalidateDependents()
                    }
            }

            // validateOnBlur
            launch {
                snapshotFlow { control.isFocused }
                    .scan(Pair(false, false)) { acc, value -> Pair(acc.second, value) }
                    // isFocused: true -> false
                    .filter { it.first && !it.second }
                    .debounce(control.fieldPolicy.validationDelay.onBlur)
                    .collect {
                        control.trigger(FieldValidateOn.Blur)
                    }
            }
        }
    }
    return control
}

@InternalSoilFormApi
internal class FormFieldController<T : Any, V, S, U>(
    private val form: FormBinding<T>,
    private val selector: (T) -> V,
    private val updater: T.(V) -> T,
    private val adapter: FieldTypeAdapter<V, S, U>,
    override val name: FieldName,
    private val dependsOn: FieldNames,
    private val rules: ValidationRuleSet<S>
) : FormFieldControl<U> {

    val fieldPolicy: FieldPolicy get() = form.policy.field

    val fieldValue: V get() = selector(form.value)

    private val meta: FieldMetaState = form[name] ?: fieldMetaStateOf(
        trigger = fieldPolicy.validationTrigger.startAt
    )

    override val value: U by derivedStateOf { adapter.toRawInput(fieldValue) }

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

    override var isEnabled: Boolean by mutableStateOf(true)

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

    override fun onValueChange(value: U) {
        form.handleChange { updater(adapter.fromRawInput(value, fieldValue)) }
        isDirty = true
    }

    override fun onFocus() {
        isFocused = true
    }

    override fun onBlur() {
        isTouched = isTouched || isFocused
        isFocused = false
    }

    override fun handleFocus(hasFocus: Boolean) {
        when {
            hasFocus && !isFocused -> onFocus()
            !hasFocus && isFocused -> onBlur()
            else -> Unit
        }
    }

    override fun trigger(validateOn: FieldValidateOn) {
        if (shouldTrigger(validateOn)) {
            validate(fieldValue)
        }
    }

    fun shouldTrigger(validateOn: FieldValidateOn): Boolean {
        return validateOn == meta.trigger
    }

    private fun validate(value: V, dryRun: Boolean = false): Boolean {
        val error = rules.flatMap { it.test(adapter.toValidationTarget(value)) }
        val isValid = error.isEmpty()
        if (!dryRun) {
            meta.errors = error
            meta.trigger = fieldPolicy.validationTrigger.next(meta.trigger, isValid)
            meta.hasBeenValidated = true
        }
        return isValid
    }

    fun revalidateDependents() {
        form.revalidateDependents(name)
    }
}
