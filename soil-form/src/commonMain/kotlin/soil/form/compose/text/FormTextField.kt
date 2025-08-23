// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose.text

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.currentCompositeKeyHash
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
import soil.form.FieldValidationMode
import soil.form.FieldValidator
import soil.form.annotation.InternalSoilFormApi
import soil.form.compose.BasicFormField
import soil.form.compose.FieldMetaState
import soil.form.compose.Form
import soil.form.compose.FormBinding
import soil.form.noFieldError

/**
 * A control interface for managing form text field state with built-in [TextFieldState] integration.
 *
 * This interface provides seamless integration with Compose's TextFieldState API, offering
 * automatic validation, focus management, and error handling for text input fields.
 *
 * Usage:
 * ```kotlin
 * form.Field(
 *     selector = { it.emailState },
 *     render = { field ->
 *         TextField(
 *             state = field.state,
 *             isError = field.hasError,
 *             modifier = Modifier.onFocusChanged { state ->
 *                 field.handleFocus(state.isFocused)
 *             }
 *         )
 *     }
 * )
 * ```
 */
@Stable
interface FormTextField : BasicFormField {
    /**
     * The TextFieldState that manages the text content and editing operations.
     * This state can be directly passed to Compose text field components.
     */
    val state: TextFieldState
}

/**
 * Creates a form text field with validation and state management for TextFieldState.
 *
 * This function creates a text field that integrates with Compose's TextFieldState API,
 * providing automatic validation of the text content and seamless integration with
 * modern text field components.
 *
 * Usage:
 * ```kotlin
 * form.Field(
 *     selector = { it.emailState },
 *     validator = FieldValidator {
 *         notBlank { "Email is required" }
 *         email { "Must be a valid email" }
 *     },
 *     render = { field ->
 *         TextField(
 *             state = field.state,
 *             isError = field.hasError,
 *             modifier = Modifier.onFocusChanged { state ->
 *                 field.handleFocus(state.isFocused)
 *             }
 *         )
 *     }
 * )
 * ```
 *
 * @param T The type of the form data.
 * @param selector A function that extracts the TextFieldState from the form data.
 * @param validator Optional validator for [TextFieldState.text].
 * @param name Optional custom name for the field. If null, an auto-generated name is used.
 * @param dependsOn Optional list of field names this field depends on for validation.
 * @param enabled Whether the field is enabled for input.
 * @param render The composable content that renders the field UI.
 */
@Composable
fun <T> Form<T>.Field(
    selector: (T) -> TextFieldState,
    validator: FieldValidator<CharSequence>? = null,
    name: FieldName? = null,
    dependsOn: FieldNames? = null,
    enabled: Boolean = true,
    render: @Composable (FormTextField) -> Unit
) {
    val control = rememberField(
        selector = selector,
        validator = validator,
        name = name,
        dependsOn = dependsOn,
        enabled = enabled
    )
    render(control)
}

/**
 * Creates a form text field with type adaptation, validation, and state management.
 *
 * This overload allows you to use a TextFieldStateAdapter to convert the text content
 * to a different type for validation. This is useful when you need to validate the
 * text as a specific data type (e.g., validating numeric input, dates, or custom formats).
 *
 * Usage:
 * ```kotlin
 * form.Field(
 *     selector = { it.ageState },
 *     adapter = IntTextFieldStateAdapter(),
 *     validator = FieldValidator<Int> {
 *         min(0) { "Age must be non-negative" }
 *         max(150) { "Age must be realistic" }
 *     },
 *     render = { field ->
 *         TextField(
 *             state = field.state,
 *             isError = field.hasError,
 *             modifier = Modifier.onFocusChanged { state ->
 *                 field.handleFocus(state.isFocused)
 *             }
 *         )
 *     }
 * )
 * ```
 *
 * @param T The type of the form data.
 * @param S The type used for validation after adaptation.
 * @param selector A function that extracts the TextFieldState from the form data.
 * @param adapter The adapter that converts text content to the validation type.
 * @param validator Optional validator for the adapted type (S).
 * @param name Optional custom name for the field. If null, an auto-generated name is used.
 * @param dependsOn Optional list of field names this field depends on for validation.
 * @param enabled Whether the field is enabled for input.
 * @param render The composable content that renders the field UI.
 */
@Composable
fun <T, S> Form<T>.Field(
    selector: (T) -> TextFieldState,
    adapter: TextFieldStateAdapter<S>,
    validator: FieldValidator<S>? = null,
    name: FieldName? = null,
    dependsOn: FieldNames? = null,
    enabled: Boolean = true,
    render: @Composable (FormTextField) -> Unit
) {
    val control = rememberField(
        selector = selector,
        adapter = adapter,
        validator = validator,
        name = name,
        dependsOn = dependsOn,
        enabled = enabled
    )
    render(control)
}

/**
 * Creates and remembers a form text field control with validation and state management.
 *
 * This function creates a FormTextField control that can be used to manage text field state
 * and interactions. Unlike the Field composable functions, this returns the control object
 * directly without rendering UI, allowing for more flexible usage patterns.
 *
 * Usage:
 * ```kotlin
 * val emailField = form.rememberField(
 *     selector = { it.emailState },
 *     validator = FieldValidator {
 *         notBlank { "Email is required" }
 *         email { "Must be a valid email" }
 *     }
 * )
 *
 * TextField(
 *     state = emailField.state,
 *     isError = field.hasError,
 *     modifier = Modifier.onFocusChanged { state ->
 *         emailField.handleFocus(state.isFocused)
 *     }
 * )
 * ```
 *
 * @param T The type of the form data.
 * @param selector A function that extracts the TextFieldState from the form data.
 * @param validator Optional validator for [TextFieldState.text].
 * @param name Optional custom name for the field. If null, an auto-generated name is used.
 * @param dependsOn Optional list of field names this field depends on for validation.
 * @param enabled Whether the field is enabled for input.
 * @return A FormTextField control object for managing the text field state and interactions.
 */
@Composable
fun <T> Form<T>.rememberField(
    selector: (T) -> TextFieldState,
    validator: FieldValidator<CharSequence>? = null,
    name: FieldName? = null,
    dependsOn: FieldNames? = null,
    enabled: Boolean = true,
): FormTextField = rememberField(
    selector = selector,
    adapter = remember { TextFieldPassthroughAdapter() },
    validator = validator,
    name = name,
    dependsOn = dependsOn,
    enabled = enabled
)

/**
 * Creates and remembers a form text field control with type adaptation, validation, and state management.
 *
 * This function creates a FormTextField control with type adaptation capabilities, allowing you to
 * convert the text content to a different type for validation. Unlike the Field composable functions,
 * this returns the control object directly without rendering UI, providing maximum flexibility for
 * custom text field implementations.
 *
 * Usage:
 * ```kotlin
 * val ageField = form.rememberField(
 *     selector = { it.ageState },
 *     adapter = IntTextFieldStateAdapter(),
 *     validator = FieldValidator<Int> {
 *         min(0) { "Age must be non-negative" }
 *         max(150) { "Age must be realistic" }
 *     }
 * )
 *
 * Column {
 *     TextField(
 *         state = ageField.state,
 *         isError = field.hasError,
 *         modifier = Modifier.onFocusChanged { state ->
 *             ageField.handleFocus(state.isFocused)
 *         }
 *     )
 * }
 * ```
 *
 * @param T The type of the form data.
 * @param S The type used for validation after adaptation.
 * @param selector A function that extracts the TextFieldState from the form data.
 * @param adapter The adapter that converts text content to the validation type.
 * @param validator Optional validator for the adapted type (S).
 * @param name Optional custom name for the field. If null, an auto-generated name is used.
 * @param dependsOn Optional list of field names this field depends on for validation.
 * @param enabled Whether the field is enabled for input.
 * @return A FormTextField control object for managing the text field state and interactions.
 */
@OptIn(InternalSoilFormApi::class, FlowPreview::class)
@Composable
fun <T, S> Form<T>.rememberField(
    selector: (T) -> TextFieldState,
    adapter: TextFieldStateAdapter<S>,
    validator: FieldValidator<S>? = null,
    name: FieldName? = null,
    dependsOn: FieldNames? = null,
    enabled: Boolean = true,
): FormTextField {
    val fieldName = name ?: auto
    val control = remember(binding) {
        FormTextFieldController(
            form = binding,
            selector = selector,
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
        return "text-field-$keyHash"
    }

// https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/runtime/runtime-saveable/src/commonMain/kotlin/androidx/compose/runtime/saveable/RememberSaveable.kt?q=MaxSupportedRadix
private const val MaxSupportedRadix: Int = 36

@InternalSoilFormApi
internal class FormTextFieldController<T, S>(
    private val form: FormBinding<T>,
    private val selector: (T) -> TextFieldState,
    private val adapter: TextFieldStateAdapter<S>,
    private val validator: FieldValidator<S>?,
    override val name: FieldName,
    private val dependsOn: FieldNames
) : FormTextField {

    private val meta: FieldMetaState = form[name] ?: FieldMetaState(
        mode = options.validationStrategy.initial
    )

    override val state: TextFieldState get() = selector(form.state.value)

    val options: FieldOptions get() = form.policy.fieldOptions

    val validationTarget: S get() = adapter.toValidationTarget(state)

    val dependentFieldChanges: Flow<FieldName> get() = form.fieldChanges.filter { it in dependsOn }

    override var error: FieldError
        get() = meta.error
        set(value) {
            meta.error = value
        }

    override var isTouched: Boolean
        get() = meta.isTouched
        set(value) {
            meta.isTouched = value
        }

    override var isFocused: Boolean by mutableStateOf(false)

    override var isEnabled: Boolean by mutableStateOf(true)

    fun register() {
        form.register(name) { value, dryRun ->
            validate(adapter.toValidationTarget(selector(value)), dryRun)
        }
        if (form[name] == null) {
            form[name] = meta
        }
    }

    fun unregister() {
        form.unregister(name)
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
            validate(adapter.toValidationTarget(state))
            true
        } else {
            false
        }
    }

    fun shouldTrigger(mode: FieldValidationMode): Boolean {
        return mode == meta.mode
    }

    private fun validate(value: S, dryRun: Boolean = false): Boolean {
        val error = validator?.invoke(value) ?: noFieldError
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
            validate(adapter.toValidationTarget(state))
        }
    }

    suspend fun notifyFormChange() {
        form.notifyFieldChange(name)
    }
}
