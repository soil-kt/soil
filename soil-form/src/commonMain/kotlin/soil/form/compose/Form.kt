package soil.form.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.autoSaver
import soil.form.FieldName
import soil.form.FieldNames
import soil.form.FormData
import soil.form.FormPolicy
import soil.form.FormRule
import soil.form.FormRules
import soil.form.annotation.InternalSoilFormApi

@Stable
interface Form<T : Any> : HasFormBinding<T> {
    val state: FormData<T>
}

@Composable
fun <T : Any> rememberForm(
    initialValue: T,
    saver: Saver<T, Any> = autoSaver(),
    policy: FormPolicy = FormPolicy.Default,
    onSubmit: (T) -> Unit
): Form<T> = rememberForm(
    state = rememberFormState(initialValue, saver, policy),
    onSubmit = onSubmit
)

@Composable
fun <T : Any> rememberForm(
    state: FormState<T>,
    onSubmit: (T) -> Unit
): Form<T> = remember(state) {
    FormController(state = state, onSubmit = onSubmit)
}

@OptIn(InternalSoilFormApi::class)
internal class FormController<T : Any>(
    override val state: FormState<T>,
    private val onSubmit: (T) -> Unit
) : Form<T>, FormBinding<T> {

    override val binding: FormBinding<T> = this

    private val _rules = mutableStateMapOf<FieldName, FormRule<T>>()
    override val rules: FormRules<T> = _rules

    private val dependencies = mutableStateMapOf<FieldName, FieldNames>()

    private val watchers by derivedStateOf {
        dependencies.keys.flatMap { key -> dependencies[key]?.map { Pair(key, it) } ?: emptyList() }
            .groupBy(keySelector = { it.second }, valueTransform = { it.first })
            .mapValues { (_, value) -> value.toSet() }
    }

    // ----- FormBinding ----- //

    override val value: T get() = state.value

    override val policy: FormPolicy get() = state.policy

    override fun get(name: FieldName): FieldMetaState? {
        return state.meta.fields[name]
    }

    override fun set(name: FieldName, fieldMeta: FieldMetaState) {
        state.meta.fields[name] = fieldMeta
    }

    override fun register(name: FieldName, dependsOn: FieldNames, rule: FormRule<T>) {
        _rules[name] = rule
        dependencies[name] = dependsOn
    }

    override fun unregister(name: FieldName) {
        _rules.remove(name)
        dependencies.remove(name)
    }

    override fun revalidateDependents(name: FieldName) {
        watchers[name]?.forEach { watcher ->
            val hasBeenValidated = state.meta.fields[watcher]?.hasBeenValidated ?: false
            if (hasBeenValidated) {
                rules[watcher]?.test(state.value, dryRun = false)
            }
        }
    }

    override fun handleChange(updater: T.() -> T) {
        state.value = with(state.value) { updater() }
    }

    override fun handleSubmit() {
        if (policy.submission.validate(state.value, rules, false)) {
            onSubmit(state.value)
        }
    }
}
