// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

/**
 * Exception used for handling validation errors during the submission process.
 *
 * This exception is useful when the validation logic is on the API side and validation errors can be detected from the response's status code.
 * By passing a mapping of field names to error messages in [errors], you can identify the fields where errors occurred.
 *
 * @property errors Mapping of validation error information.
 * @property cause The exception source that caused this exception.
 * @constructor Creates a new instance with specified validation error information.
 */
@Deprecated("Please migrate to the new form implementation. This legacy code will be removed in a future version.")
class FormValidationException(
    val errors: FormErrors,
    cause: Throwable? = null
) : RuntimeException("Unverified form ${errors.keys}", cause)
