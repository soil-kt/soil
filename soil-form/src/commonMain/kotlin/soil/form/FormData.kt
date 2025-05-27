package soil.form

interface FormData<T> {
    val value: T
    val meta: FormMeta
}

interface FormMeta {
    val fields: Map<FieldName, FieldMeta>
    val canSubmit: Boolean
}

interface FieldMeta {
    val error: FieldError
    val mode: FieldValidationMode
    val isDirty: Boolean
    val isTouched: Boolean
    val isValidated: Boolean
}
