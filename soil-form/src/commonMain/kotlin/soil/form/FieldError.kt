package soil.form

/**
 * Represents a single error message in the field.
 */
typealias FieldError = String

/**
 * Represents multiple error messages in the field.
 */
typealias FieldErrors = List<FieldError>

/**
 * Creates error messages for a field.
 *
 * @param messages Error messages. There must be at least one error message.
 * @return The generated error messages for the field.
 */
fun fieldError(vararg messages: String): FieldErrors {
    require(messages.isNotEmpty())
    return listOf(*messages)
}

/**
 * Syntax sugar representing that there are no errors in the field.
 */
val noErrors: FieldErrors = emptyList()
