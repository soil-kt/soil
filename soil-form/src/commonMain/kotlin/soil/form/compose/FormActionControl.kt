package soil.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import soil.form.FieldNames
import soil.form.SubmissionPolicy
import soil.form.annotation.InternalSoilFormApi

@Stable
interface FormActionControl<T> {
    val canSubmit: Boolean

    fun submit()
}

@OptIn(FlowPreview::class, InternalSoilFormApi::class)
@Composable
internal fun <T : Any> Form<T>.rememberFormActionControl(): FormActionControl<T> {
    val control = remember(binding) {
        FormActionController(binding)
    }
    if (control.policy.preValidation) {
        LaunchedEffect(control) {
            // validateOnMount
            launch {
                snapshotFlow { control.fields }
                    .debounce(control.policy.preValidationDelay.onMount)
                    .collect {
                        control.preValidate(value = state.value)
                    }
            }

            // validateOnChange
            launch {
                snapshotFlow { state.value }
                    .debounce(control.policy.preValidationDelay.onChange)
                    .collect {
                        control.preValidate(value = it)
                    }
            }
        }
    }
    return control
}

@InternalSoilFormApi
internal class FormActionController<T : Any>(
    private val form: FormBinding<T>
) : FormActionControl<T> {

    val policy: SubmissionPolicy get() = form.policy.submission
    val fields: FieldNames get() = form.rules.keys

    override var canSubmit: Boolean by mutableStateOf(!policy.preValidation)

    fun preValidate(value: T) {
        canSubmit = policy.validate(value = value, rules = form.rules, dryRun = true)
    }

    override fun submit() {
        form.handleSubmit()
    }
}
