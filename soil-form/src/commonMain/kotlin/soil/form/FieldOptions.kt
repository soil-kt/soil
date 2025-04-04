// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Configuration options for field-level validation behavior.
 *
 * This interface defines how and when validation should be performed for individual form fields,
 * including validation timing strategies and debounce delays for different validation triggers.
 *
 * Usage:
 * ```kotlin
 * val fieldOptions = FieldOptions(
 *     validationStrategy = FieldValidationStrategy(
 *         initial = FieldValidationMode.Blur,
 *         next = { current, isValid ->
 *             if (isValid) FieldValidationMode.Blur else FieldValidationMode.Change
 *         }
 *     ),
 *     validationDelayOnChange = 300.milliseconds
 * )
 * ```
 */
interface FieldOptions {
    /**
     * The validation strategy that determines when validation should be triggered.
     */
    val validationStrategy: FieldValidationStrategy

    /**
     * The delay before validation is triggered when a field is first mounted.
     */
    val validationDelayOnMount: Duration

    /**
     * The delay before validation is triggered when a field value changes.
     * This helps prevent excessive validation calls during rapid typing.
     */
    val validationDelayOnChange: Duration

    /**
     * The delay before validation is triggered when a field loses focus.
     */
    val validationDelayOnBlur: Duration

    /**
     * Default field options with sensible defaults for most use cases.
     */
    companion object Default : FieldOptions {
        override val validationStrategy: FieldValidationStrategy = FieldValidationStrategy()
        override val validationDelayOnMount: Duration = Duration.ZERO
        override val validationDelayOnChange: Duration = 250.milliseconds
        override val validationDelayOnBlur: Duration = Duration.ZERO
    }
}

/**
 * Creates a [FieldOptions] instance with the specified configuration.
 *
 * This factory function allows you to customize field validation behavior by specifying
 * validation strategies and timing delays. All parameters have sensible defaults.
 *
 * Usage:
 * ```kotlin
 * val customFieldOptions = FieldOptions(
 *     validationStrategy = FieldValidationStrategy(
 *         initial = FieldValidationMode.Change,
 *         next = { _, _ -> FieldValidationMode.Change }
 *     ),
 *     validationDelayOnChange = 500.milliseconds,
 *     validationDelayOnBlur = 100.milliseconds
 * )
 * ```
 *
 * @param validationStrategy The validation strategy to use. Defaults to the standard strategy.
 * @param validationDelayOnMount The delay before validation on field mount. Defaults to no delay.
 * @param validationDelayOnChange The delay before validation on value change. Defaults to 250ms.
 * @param validationDelayOnBlur The delay before validation on field blur. Defaults to no delay.
 * @return A [FieldOptions] instance with the specified configuration.
 */
fun FieldOptions(
    validationStrategy: FieldValidationStrategy = FieldOptions.validationStrategy,
    validationDelayOnMount: Duration = FieldOptions.validationDelayOnMount,
    validationDelayOnChange: Duration = FieldOptions.validationDelayOnChange,
    validationDelayOnBlur: Duration = FieldOptions.validationDelayOnBlur
): FieldOptions {
    return object : FieldOptions {
        override val validationStrategy: FieldValidationStrategy = validationStrategy
        override val validationDelayOnMount: Duration = validationDelayOnMount
        override val validationDelayOnChange: Duration = validationDelayOnChange
        override val validationDelayOnBlur: Duration = validationDelayOnBlur
    }
}
