// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import soil.form.FieldError
import soil.form.FieldName
import soil.form.FieldNames
import soil.form.FieldOptions
import soil.form.FieldPassthroughAdapter
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
 *     updater = { copy(email = it) },
 *     render = { field ->
 *         TextField(
 *             value = field.value,
 *             onValueChange = field::onValueChange,
 *             isError = field.hasError,
 *             modifier = Modifier.onFocusChanged { state ->
 *                 field.handleFocus(state.isFocused)
 *             }
 *         )
 *     }
 * )
 * ```
 *
 * @param V The type of the field value.
 */
@Stable
interface FormField<V> {
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
     * @return True if validation was triggered, false if it was not needed.
     */
    fun trigger(mode: FieldValidationMode): Boolean
}

/**
 * Whether the field currently has any validation errors.
 *
 * This is a convenience property that returns true when the field has validation
 * error messages, false otherwise. It's commonly used to conditionally apply
 * error styling to UI components.
 */
val FormField<*>.hasError: Boolean get() = error != noFieldError

/**
 * Creates a form field with validation and state management.
 *
 * This function creates a form field that automatically handles value changes,
 * validation, and state management. The field integrates with the parent form
 * and participates in form-wide validation.
 *
 * Usage:
 * ```kotlin
 * form.Field(
 *     selector = { it.email },
 *     updater = { copy(email = it) },
 *     validator = FieldValidator {
 *         notBlank { "Email is required" }
 *         email { "Must be a valid email" }
 *     },
 *     render = { field ->
 *         TextField(
 *             value = field.value, // String type
 *             onValueChange = field::onValueChange,
 *             isError = field.hasError
 *         )
 *     }
 * )
 * ```
 *
 * @param T The type of the form data.
 * @param V The type of the field value.
 * @param selector A function that extracts the field value from the form data.
 * @param updater A function that updates the form data with a new field value.
 * @param validator Optional validator for the field value.
 * @param name Optional custom name for the field. If null, an auto-generated name is used.
 * @param dependsOn Optional list of field names this field depends on for validation.
 * @param enabled Whether the field is enabled for input.
 * @param render The composable content that renders the field UI.
 */
@Composable
fun <T, V> Form<T>.Field(
    selector: (T) -> V,
    updater: T.(V) -> T,
    validator: FieldValidator<V>? = null,
    name: FieldName? = null,
    dependsOn: FieldNames? = null,
    enabled: Boolean = true,
    render: @Composable (FormField<V>) -> Unit
) {
    val control = rememberField(
        selector = selector,
        updater = updater,
        validator = validator,
        name = name,
        dependsOn = dependsOn,
        enabled = enabled
    )
    render(control)
}

/**
 * Creates a form field with type adaptation, validation, and state management.
 *
 * This overload allows you to use a type adapter to convert between the stored
 * value type (V), validation target type (S), and input type (U). This is useful
 * when you need to validate or display a field value in a different format than
 * how it's stored.
 *
 * Usage:
 * ```kotlin
 * form.Field(
 *     selector = { it.age },
 *     updater = { copy(age = it) },
 *     adapter = IntStringAdapter(),
 *     validator = FieldValidator<Int> {
 *         min(0) { "Age must be non-negative" }
 *         max(150) { "Age must be realistic" }
 *     },
 *     render = { field ->
 *         TextField(
 *             value = field.value, // String type
 *             onValueChange = field::onValueChange
 *         )
 *     }
 * )
 * ```
 *
 * @param T The type of the form data.
 * @param V The type of the field value as stored in the form data.
 * @param S The type used for validation.
 * @param U The type used for input/display.
 * @param selector A function that extracts the field value from the form data.
 * @param updater A function that updates the form data with a new field value.
 * @param adapter The type adapter that converts between V, S, and U types.
 * @param validator Optional validator for the validation target type (S).
 * @param name Optional custom name for the field. If null, an auto-generated name is used.
 * @param dependsOn Optional list of field names this field depends on for validation.
 * @param enabled Whether the field is enabled for input.
 * @param render The composable content that renders the field UI.
 */
@Composable
fun <T, V, S, U> Form<T>.Field(
    selector: (T) -> V,
    updater: T.(V) -> T,
    adapter: FieldTypeAdapter<V, S, U>,
    validator: FieldValidator<S>? = null,
    name: FieldName? = null,
    dependsOn: FieldNames? = null,
    enabled: Boolean = true,
    render: @Composable (FormField<U>) -> Unit
) {
    val control = rememberField(
        selector = selector,
        updater = updater,
        adapter = adapter,
        validator = validator,
        name = name,
        dependsOn = dependsOn,
        enabled = enabled
    )
    render(control)
}

/**
 * Creates and remembers a form field control with validation and state management.
 *
 * This function creates a FormField control that can be used to manage field state
 * and interactions. Unlike the Field composable functions, this returns the control
 * object directly without rendering UI, allowing for more flexible usage patterns.
 *
 * Usage:
 * ```kotlin
 * val emailField = form.rememberField(
 *     selector = { it.email },
 *     updater = { copy(email = it) },
 *     validator = FieldValidator {
 *         notBlank { "Email is required" }
 *         email { "Must be a valid email" }
 *     }
 * )
 *
 * TextField(
 *     value = emailField.value,
 *     onValueChange = emailField::onValueChange,
 *     isError = emailField.hasError
 * )
 * ```
 *
 * @param T The type of the form data.
 * @param V The type of the field value.
 * @param selector A function that extracts the field value from the form data.
 * @param updater A function that updates the form data with a new field value.
 * @param validator Optional validator for the field value.
 * @param name Optional custom name for the field. If null, an auto-generated name is used.
 * @param dependsOn Optional list of field names this field depends on for validation.
 * @param enabled Whether the field is enabled for input.
 * @return A FormField control object for managing the field state and interactions.
 */
@Composable
fun <T, V> Form<T>.rememberField(
    selector: (T) -> V,
    updater: T.(V) -> T,
    validator: FieldValidator<V>? = null,
    name: FieldName? = null,
    dependsOn: FieldNames? = null,
    enabled: Boolean = true,
): FormField<V> = rememberField(
    selector = selector,
    updater = updater,
    adapter = remember { FieldPassthroughAdapter() },
    validator = validator,
    name = name,
    dependsOn = dependsOn,
    enabled = enabled
)

/**
 * Creates and remembers a form field control with type adaptation, validation, and state management.
 *
 * This function creates a FormField control with type adaptation capabilities, allowing you to
 * convert between the stored value type (V), validation target type (S), and input type (U).
 * Unlike the Field composable functions, this returns the control object directly without
 * rendering UI, providing maximum flexibility for custom field implementations.
 *
 * Usage:
 * ```kotlin
 * val ageField = form.rememberField(
 *     selector = { it.age },
 *     updater = { copy(age = it) },
 *     adapter = IntStringAdapter(),
 *     validator = FieldValidator<Int> {
 *         min(0) { "Age must be non-negative" }
 *         max(150) { "Age must be realistic" }
 *     }
 * )
 *
 * TextField(
 *     value = ageField.value, // String type from adapter
 *     onValueChange = ageField::onValueChange,
 *     isError = ageField.hasError
 * )
 * ```
 *
 * @param T The type of the form data.
 * @param V The type of the field value as stored in the form data.
 * @param S The type used for validation.
 * @param U The type used for input/display.
 * @param selector A function that extracts the field value from the form data.
 * @param updater A function that updates the form data with a new field value.
 * @param adapter The type adapter that converts between V, S, and U types.
 * @param validator Optional validator for the validation target type (S).
 * @param name Optional custom name for the field. If null, an auto-generated name is used.
 * @param dependsOn Optional list of field names this field depends on for validation.
 * @param enabled Whether the field is enabled for input.
 * @return A FormField control object for managing the field state and interactions.
 */
@OptIn(InternalSoilFormApi::class, FlowPreview::class)
@Composable
fun <T, V, S, U> Form<T>.rememberField(
    selector: (T) -> V,
    updater: T.(V) -> T,
    adapter: FieldTypeAdapter<V, S, U>,
    validator: FieldValidator<S>? = null,
    name: FieldName? = null,
    dependsOn: FieldNames? = null,
    enabled: Boolean = true,
): FormField<U> {
    val fieldName = name ?: auto
    val control = remember(binding) {
        FormFieldController(
            form = binding,
            selector = selector,
            updater = updater,
            adapter = adapter,
            validator = validator,
            name = fieldName,
            dependsOn = dependsOn.orEmpty(),
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
                    .drop(1) // Skip the initial value
                    .onEach { control.notifyFormChange() }
                    .debounce(control.options.validationDelayOnChange)
                    .collect {
                        control.trigger(FieldValidationMode.Change)
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

            // revalidate
            launch {
                control.dependentFieldChanges
                    .debounce(control.options.validationDelayOnChange)
                    .collect {
                        control.revalidateIfNeeded()
                    }
            }
        }
    }
    return control
}

private val auto: FieldName
    @Composable
    get() {
        val keyHash = currentCompositeKeyHash.toString(MaxSupportedRadix)
        return "field-$keyHash"
    }

// https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/runtime/runtime-saveable/src/commonMain/kotlin/androidx/compose/runtime/saveable/RememberSaveable.kt?q=MaxSupportedRadix
private const val MaxSupportedRadix: Int = 36


@InternalSoilFormApi
internal class FormFieldController<T, V, S, U>(
    private val form: FormBinding<T>,
    private val selector: (T) -> V,
    private val updater: T.(V) -> T,
    private val adapter: FieldTypeAdapter<V, S, U>,
    private val validator: FieldValidator<S>?,
    override val name: FieldName,
    private val dependsOn: FieldNames
) : FormField<U> {

    private val rawValue: V get() = selector(form.state.value)

    private val meta: FieldMetaState = form[name] ?: FieldMetaState(
        mode = options.validationStrategy.initial
    )

    override val value: U by derivedStateOf { adapter.toInput(rawValue) }

    val options: FieldOptions get() = form.policy.fieldOptions

    val validationTarget: S get() = adapter.toValidationTarget(rawValue)

    val dependentFieldChanges: Flow<FieldName> get() = form.fieldChanges.filter { it in dependsOn }

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
        form.register(name) { value, dryRun -> validate(selector(value), dryRun) }
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

    override fun trigger(mode: FieldValidationMode): Boolean {
        return if (shouldTrigger(mode)) {
            validate(rawValue)
            true
        } else {
            false
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

    fun revalidateIfNeeded() {
        if (meta.isValidated) {
            validate(rawValue)
        }
    }

    suspend fun notifyFormChange() {
        form.notifyFieldChange(name)
    }
}
