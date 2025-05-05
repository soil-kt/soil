package soil.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import soil.form.annotation.InternalSoilFormApi

@OptIn(FlowPreview::class, InternalSoilFormApi::class)
@Composable
fun <T : Any> Form<T>.Action(
    content: @Composable (FormActionControl<T>) -> Unit
) {
    val control = remember(binding) {
        FormActionController(binding)
    }
    content(control)
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
}
