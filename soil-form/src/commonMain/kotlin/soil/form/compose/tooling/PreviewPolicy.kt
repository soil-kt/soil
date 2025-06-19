// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.compose.tooling

import soil.form.FieldOptions
import soil.form.FieldValidationMode
import soil.form.FieldValidationStrategy
import soil.form.FormOptions
import soil.form.compose.FormPolicy

/**
 * A specialized form policy designed for use in Compose Previews and testing environments.
 *
 * This policy provides a simplified configuration optimized for previewing individual
 * form fields in isolation. It focuses on essential validation behavior while avoiding
 * complex form-wide interactions that aren't relevant in preview contexts.
 *
 * The policy is used internally by [PreviewField] to create a minimal form environment
 * that supports field validation and state management without the overhead of a full
 * production form setup.
 */
interface PreviewPolicy : FormPolicy

/**
 * Creates a preview policy with the specified initial validation mode.
 *
 * This factory function creates a [PreviewPolicy] instance configured for use in
 * Compose Previews and testing. The policy determines when field validation should
 * be triggered during the preview lifecycle.
 *
 * The validation strategy is simplified compared to production forms:
 * - Initial validation occurs based on the specified mode
 * - Subsequent validation always occurs on value changes
 * - No complex form-wide validation dependencies
 *
 * Usage:
 * ```kotlin
 * // Validate immediately when the field is mounted (useful for testing error states)
 * val mountPolicy = PreviewPolicy(PreviewValidationMode.Mount)
 *
 * // Validate only when the field value changes (default behavior)
 * val changePolicy = PreviewPolicy(PreviewValidationMode.Change)
 * ```
 *
 * @param initialMode The validation mode that determines when initial validation occurs.
 * @return A configured PreviewPolicy instance.
 */
fun PreviewPolicy(initialMode: PreviewValidationMode = PreviewValidationMode.Change): PreviewPolicy {
    val fieldValidationMode = when (initialMode) {
        PreviewValidationMode.Mount -> FieldValidationMode.Mount
        PreviewValidationMode.Change -> FieldValidationMode.Change
    }
    return object : PreviewPolicy {
        override val formOptions: FormOptions = FormOptions()
        override val fieldOptions: FieldOptions = FieldOptions(
            validationStrategy = FieldValidationStrategy(fieldValidationMode) { _, _ ->
                FieldValidationMode.Change
            }
        )
    }
}

/**
 * Defines when validation should be triggered for fields in preview environments.
 *
 * This enum provides options for controlling the initial validation timing in
 * [PreviewField] components, allowing developers to test different validation
 * scenarios during development.
 */
enum class PreviewValidationMode {
    /**
     * Validation is triggered immediately when the field is mounted/created.
     *
     * This mode is useful for:
     * - Testing error states in previews
     * - Validating required fields that should show errors immediately
     * - Previewing fields with initial validation requirements
     */
    Mount,

    /**
     * Validation is triggered only when the field value changes.
     *
     * This mode is useful for:
     * - Testing normal user interaction flows
     * - Previewing fields that should only validate after user input
     * - Default preview behavior that matches typical form usage
     */
    Change;
}
