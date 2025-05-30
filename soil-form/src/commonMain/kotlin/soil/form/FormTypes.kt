// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

@Deprecated("Legacy")
typealias FormFieldNames = Set<FieldName>
@Deprecated("Legacy")
typealias FormFieldDependencies = Map<FieldName, FormFieldNames>
@Deprecated("Legacy")
typealias FormErrors = Map<FieldName, FieldErrors>
@Deprecated("Legacy")
typealias FormTriggers = Map<FieldName, FieldValidateOn>
@Deprecated("Legacy")
typealias FormRules<T> = Map<FieldName, FormRule<T>>
