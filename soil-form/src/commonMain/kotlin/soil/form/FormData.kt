package soil.form

interface FormData<T : Any> {
    val value: T
    val meta: FormMeta<T>
}

interface FormMeta<T : Any> {
    val fields: Map<FieldName, FieldMeta>
    val defaultValue: T
}

interface FieldMeta {
    val error: FieldError
    val trigger: FieldValidateOn
    val isDirty: Boolean
    val isTouched: Boolean
    val hasBeenValidated: Boolean
}
