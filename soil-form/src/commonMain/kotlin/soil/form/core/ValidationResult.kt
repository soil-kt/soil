package soil.form.core

sealed class ValidationResult {
    data object Valid : ValidationResult()
    data class Invalid(val messages: List<String>) : ValidationResult() {
        constructor(message: String) : this(listOf(message))
    }
}
