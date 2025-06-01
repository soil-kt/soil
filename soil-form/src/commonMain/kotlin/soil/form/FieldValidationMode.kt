// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

/**
 * Defines when field validation should be triggered.
 *
 * This enum represents different events that can trigger field validation,
 * allowing for flexible validation timing strategies.
 */
enum class FieldValidationMode {

    /**
     * Validation triggered when the field is first mounted/rendered.
     */
    Mount,

    /**
     * Validation triggered when the field value changes.
     */
    Change,

    /**
     * Validation triggered when the field loses focus.
     */
    Blur,

    /**
     * Validation triggered when the form is submitted.
     */
    Submit
}

/**
 * Defines a strategy for determining when field validation should occur.
 *
 * This interface allows customization of validation timing behavior by defining
 * the initial validation mode and how the validation mode should change based
 * on the current mode and validation result.
 *
 * Usage:
 * ```kotlin
 * val strategy = FieldValidationStrategy(
 *     initial = FieldValidationMode.Blur,
 *     next = { current, isValid ->
 *         if (isValid) FieldValidationMode.Blur
 *         else FieldValidationMode.Change
 *     }
 * )
 * ```
 */
interface FieldValidationStrategy {
    /**
     * The initial validation mode when a field is first created.
     */
    val initial: FieldValidationMode

    /**
     * Determines the next validation mode based on the current mode and validation result.
     *
     * @param current The current validation mode.
     * @param isValid Whether the last validation passed.
     * @return The next validation mode to use.
     */
    fun next(current: FieldValidationMode, isValid: Boolean): FieldValidationMode
}

/**
 * Creates a field validation strategy with the specified initial mode and transition logic.
 *
 * This factory function provides a convenient way to create custom validation strategies
 * without implementing the interface directly.
 *
 * Usage:
 * ```kotlin
 * // Strategy that validates on blur initially, then on change after first error
 * val strategy = FieldValidationStrategy(
 *     initial = FieldValidationMode.Blur,
 *     next = { current, isValid ->
 *         when {
 *             isValid -> FieldValidationMode.Blur
 *             else -> FieldValidationMode.Change
 *         }
 *     }
 * )
 * ```
 *
 * @param initial The initial validation mode. Defaults to [FieldValidationMode.Blur].
 * @param next A function that determines the next validation mode based on current mode and validation result.
 * @return A [FieldValidationStrategy] implementation.
 */
fun FieldValidationStrategy(
    initial: FieldValidationMode = FieldValidationMode.Blur,
    next: (current: FieldValidationMode, isValid: Boolean) -> FieldValidationMode = defaultStrategy
): FieldValidationStrategy {
    return object : FieldValidationStrategy {
        override val initial: FieldValidationMode = initial

        override fun next(current: FieldValidationMode, isValid: Boolean): FieldValidationMode {
            return next(current, isValid)
        }
    }
}

private val defaultStrategy: (current: FieldValidationMode, isValid: Boolean) -> FieldValidationMode =
    { current, isValid ->
        when (current) {
            FieldValidationMode.Mount -> if (isValid) FieldValidationMode.Blur else FieldValidationMode.Change
            FieldValidationMode.Blur,
            FieldValidationMode.Change,
            FieldValidationMode.Submit -> FieldValidationMode.Change
        }
    }
