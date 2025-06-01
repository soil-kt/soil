// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

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
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import soil.form.FieldError
import soil.form.FieldName
import soil.form.FieldNames
import soil.form.FieldOptions
import soil.form.FieldTypeAdapter
import soil.form.FieldValidationMode
import soil.form.FieldValidator
import soil.form.annotation.InternalSoilFormApi
import soil.form.noFieldError

/**
 * A control interface for managing individual form field state and interactions.
 *
 * This interface provides access to field state (value, validation errors, focus state)
 * and methods for handling user interactions (value changes, focus events, validation triggers).
 * It serves as the bridge between the form field UI and the underlying form state management.
 *
 * Usage:
 * ```kotlin
 * form.Field(
 *     selector = { it.email },
 *     updater = { copy(email = it) }
 * ) { fieldControl ->
 *     TextField(
 *         value = fieldControl.value,
 *         onValueChange = fieldControl::onValueChange,
 *         isError = fieldControl.hasError,
 *         modifier = Modifier.onFocusChanged { state ->
 *             fieldControl.handleFocus(state.isFocused)
 *         }
 *     )
 * }
 * ```
 *
 * @param V The type of the field value.
 */
@Stable
interface FormFieldControl<V> {
    /**
     * The unique name identifier for this field within the form.
     */
    val name: FieldName

    /**
     * The current value of the field.
     */
    val value: V

    /**
     * The current validation error for this field, if any.
     */
    val error: FieldError

    /**
     * Whether the field value has been modified from its initial value.
     */
    val isDirty: Boolean

    /**
     * Whether the field has been touched (focused and then blurred) by the user.
     */
    val isTouched: Boolean

    /**
     * Whether the field currently has focus.
     */
    val isFocused: Boolean

    /**
     * Whether the field is enabled for user interaction.
     */
    val isEnabled: Boolean

    /**
     * Updates the field value and triggers validation if configured.
     *
     * @param value The new value for the field.
     */
    fun onValueChange(value: V)

    /**
     * Marks the field as focused.
     */
    fun onFocus()

    /**
     * Marks the field as blurred (not focused) and touched.
     */
    fun onBlur()

    /**
     * Handles focus state changes by calling onFocus() or onBlur() as appropriate.
     *
     * @param hasFocus Whether the field currently has focus.
     */
    fun handleFocus(hasFocus: Boolean)

    /**
     * Manually triggers validation for this field with the specified mode.
     *
     * @param mode The validation mode to trigger.
     */
    fun trigger(mode: FieldValidationMode)
}

/**
 * Extension property that returns true if the field has any validation errors.
 *
 * This is a convenient way to check if a field has validation errors without
 * having to compare against [noFieldError] directly.
 *
 * Usage:
 * ```kotlin
 * TextField(
 *     value = fieldControl.value,
 *     onValueChange = fieldControl::onValueChange,
 *     isError = fieldControl.hasError
 * )
 * ```
 */
inline val FormFieldControl<*>.hasError
    get() = error != noFieldError


@OptIn(InternalSoilFormApi::class, FlowPreview::class)
@Composable
internal fun <T, V, S, U> Form<T>.rememberFieldControl(
    selector: (T) -> V,
    updater: T.(V) -> T,
    adapter: FieldTypeAdapter<V, S, U>,
    validator: FieldValidator<S>?,
    name: FieldName,
    dependsOn: FieldNames,
    enabled: Boolean,
): FormFieldControl<U> {
    val control = remember(binding) {
        FormFieldController(
            form = binding,
            selector = selector,
            updater = updater,
            adapter = adapter,
            validator = validator,
            name = name,
            dependsOn = dependsOn,
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
                snapshotFlow { control.shouldTrigger(FieldValidationMode.Mount) }
                    .filter { it }
                    .debounce(control.options.validationDelayOnMount)
                    .collect {
                        control.trigger(FieldValidationMode.Mount)
                    }
            }

            // validateOnChange
            launch {
                snapshotFlow { control.validationTarget }
                    .debounce(control.options.validationDelayOnChange)
                    .collect {
                        control.trigger(FieldValidationMode.Change)
                        control.revalidateDependents()
                    }
            }

            // validateOnBlur
            launch {
                snapshotFlow { control.isFocused }
                    .scan(Pair(false, false)) { acc, value -> Pair(acc.second, value) }
                    // isFocused: true -> false
                    .filter { it.first && !it.second }
                    .debounce(control.options.validationDelayOnBlur)
                    .collect {
                        control.trigger(FieldValidationMode.Blur)
                    }
            }
        }
    }
    return control
}

@InternalSoilFormApi
internal class FormFieldController<T, V, S, U>(
    private val form: FormBinding<T>,
    private val selector: (T) -> V,
    private val updater: T.(V) -> T,
    private val adapter: FieldTypeAdapter<V, S, U>,
    private val validator: FieldValidator<S>?,
    override val name: FieldName,
    private val dependsOn: FieldNames
) : FormFieldControl<U> {

    private val rawValue: V get() = selector(form.value)

    private val meta: FieldMetaState = form[name] ?: FieldMetaState(
        mode = options.validationStrategy.initial
    )

    override val value: U by derivedStateOf { adapter.toInput(rawValue) }

    val options: FieldOptions get() = form.policy.fieldOptions

    val validationTarget: S get() = adapter.toValidationTarget(rawValue)

    override var error: FieldError
        get() = meta.error
        set(value) {
            meta.error = value
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
        form.handleChange { updater(adapter.fromInput(value, rawValue)) }
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

    override fun trigger(mode: FieldValidationMode) {
        if (shouldTrigger(mode)) {
            validate(rawValue)
        }
    }

    fun shouldTrigger(mode: FieldValidationMode): Boolean {
        return mode == meta.mode
    }

    private fun validate(value: V, dryRun: Boolean = false): Boolean {
        val error = validator?.invoke(adapter.toValidationTarget(value)) ?: noFieldError
        val isValid = error == noFieldError
        if (!dryRun) {
            Snapshot.withMutableSnapshot {
                meta.error = error
                meta.mode = options.validationStrategy.next(meta.mode, isValid)
                meta.isValidated = true
            }
        }
        return isValid
    }

    fun revalidateDependents() {
        form.revalidateDependents(name)
    }
}
