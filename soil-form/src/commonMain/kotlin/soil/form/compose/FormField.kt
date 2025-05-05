package soil.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.currentCompositeKeyHash
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import soil.form.FieldName
import soil.form.FieldNames
import soil.form.FieldValidateOn
import soil.form.ValidationRuleSet
import soil.form.annotation.InternalSoilFormApi

@OptIn(FlowPreview::class, InternalSoilFormApi::class)
@Composable
fun <T : Any, V> Form<T>.Field(
    selector: (T) -> V,
    updater: T.(V) -> T,
    rules: ValidationRuleSet<V> = emptySet(),
    name: FieldName? = null,
    dependsOn: FieldNames? = null,
    disabled: Boolean = false,
    content: @Composable (FormFieldControl<V>) -> Unit
) {
    val fieldName = name ?: auto
    val control = remember(binding) {
        FormFieldController(
            form = binding,
            selector = selector,
            updater = updater,
            rules = rules,
            name = fieldName,
            dependsOn = dependsOn.orEmpty()
        )
    }.apply { isDisabled = disabled }
    content(control)
    if (!disabled) {
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
                    .debounce(control.policy.validationDelay.onMount)
                    .collect {
                        control.trigger(FieldValidateOn.Mount)
                    }
            }

            // validateOnChange
            launch {
                snapshotFlow { control.value }
                    .debounce(control.policy.validationDelay.onChange)
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
                    .debounce(control.policy.validationDelay.onBlur)
                    .collect {
                        control.trigger(FieldValidateOn.Blur)
                    }
            }
        }
    }
}

private val auto: FieldName
    @Composable
    get() {
        val keyHash = currentCompositeKeyHash.toString(MaxSupportedRadix)
        return "field-$keyHash"
    }

// https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/runtime/runtime-saveable/src/commonMain/kotlin/androidx/compose/runtime/saveable/RememberSaveable.kt?q=MaxSupportedRadix
private const val MaxSupportedRadix: Int = 36
