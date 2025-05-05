package soil.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentCompositeKeyHash
import soil.form.FieldName
import soil.form.FieldNames
import soil.form.FieldPassthroughAdapter
import soil.form.FieldTypeAdapter
import soil.form.ValidationRuleSet


@Composable
fun <T : Any, V> Form<T>.Field(
    selector: (T) -> V,
    updater: T.(V) -> T,
    name: FieldName? = null,
    dependsOn: FieldNames? = null,
    rules: ValidationRuleSet<V> = emptySet(),
    enabled: Boolean = true,
    content: @Composable (FormFieldControl<V>) -> Unit
) {
    val control = rememberFormFieldControl(
        selector = selector,
        updater = updater,
        adapter = FieldPassthroughAdapter(),
        name = name ?: auto,
        dependsOn = dependsOn.orEmpty(),
        rules = rules,
        enabled = enabled
    )
    content(control)
}

@Composable
fun <T : Any, V, S, U> Form<T>.Field(
    selector: (T) -> V,
    updater: T.(V) -> T,
    adapter: FieldTypeAdapter<V, S, U>,
    name: FieldName? = null,
    dependsOn: FieldNames? = null,
    rules: ValidationRuleSet<S> = emptySet(),
    enabled: Boolean = true,
    content: @Composable (FormFieldControl<U>) -> Unit
) {
    val control = rememberFormFieldControl(
        selector = selector,
        updater = updater,
        adapter = adapter,
        rules = rules,
        name = name ?: auto,
        dependsOn = dependsOn.orEmpty(),
        enabled = enabled
    )
    content(control)
}

private val auto: FieldName
    @Composable
    get() {
        val keyHash = currentCompositeKeyHash.toString(MaxSupportedRadix)
        return "field-$keyHash"
    }

// https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/runtime/runtime-saveable/src/commonMain/kotlin/androidx/compose/runtime/saveable/RememberSaveable.kt?q=MaxSupportedRadix
private const val MaxSupportedRadix: Int = 36
