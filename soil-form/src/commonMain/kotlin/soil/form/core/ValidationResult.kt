package soil.form.core

sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val errors: List<String>) : ValidationResult() {
        constructor(error: String) : this(listOf(error))
    }
}
