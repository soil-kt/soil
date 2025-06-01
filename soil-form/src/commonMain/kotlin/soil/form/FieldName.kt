// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

/**
 * A type alias for field names used to uniquely identify fields within a form.
 *
 * Field names are string identifiers that allow the form system to track and manage
 * individual fields. They are used for validation, error reporting, and dependency
 * management between fields.
 *
 * Usage:
 * ```kotlin
 * form.Field(
 *     name = "email", // FieldName
 *     selector = { it.email },
 *     updater = { copy(email = it) }
 * ) { fieldControl ->
 *     // Field UI
 * }
 * ```
 */
typealias FieldName = String

/**
 * A type alias for a set of field names.
 *
 * This is used to specify dependencies between fields, where one field's validation
 * may depend on the values or states of other fields.
 *
 * Usage:
 * ```kotlin
 * form.Field(
 *     name = "confirmPassword",
 *     dependsOn = setOf("password"), // FieldNames
 *     selector = { it.confirmPassword },
 *     updater = { copy(confirmPassword = it) }
 * ) { fieldControl ->
 *     // Field UI
 * }
 * ```
 */
typealias FieldNames = Set<FieldName>
