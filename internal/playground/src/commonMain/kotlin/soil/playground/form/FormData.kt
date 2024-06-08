package soil.playground.form

import kotlinx.serialization.Serializable

// The form input fields are based on the Live Demo used in React Hook Form.
// You can reference it here: https://react-hook-form.com/

@Serializable
data class FormData(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val mobileNumber: String = "",
    val title: Title? = null,
    val developer: Boolean? = null
)

enum class Title {
    Mr,
    Mrs,
    Miss,
    Dr,
}
