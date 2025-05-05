package soil.form.compose

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import soil.form.FieldNames
import soil.form.SubmissionPolicy
import soil.form.annotation.InternalSoilFormApi

@Stable
interface FormActionControl<T> {
    val canSubmit: Boolean

    fun submit()
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
