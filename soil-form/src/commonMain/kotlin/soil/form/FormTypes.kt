// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

typealias FormFieldNames = Set<FieldName>
typealias FormFieldDependencies = Map<FieldName, FormFieldNames>
typealias FormErrors = Map<FieldName, FieldErrors>
typealias FormTriggers = Map<FieldName, FieldValidateOn>
typealias FormRules<T> = Map<FieldName, FormRule<T>>
