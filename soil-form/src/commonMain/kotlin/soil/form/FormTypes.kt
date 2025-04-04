// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form

@Deprecated("Please migrate to the new form implementation. This legacy code will be removed in a future version.")
typealias FormFieldNames = Set<FieldName>
@Deprecated("Please migrate to the new form implementation. This legacy code will be removed in a future version.")
typealias FormFieldDependencies = Map<FieldName, FormFieldNames>
@Deprecated("Please migrate to the new form implementation. This legacy code will be removed in a future version.")
typealias FormErrors = Map<FieldName, FieldErrors>
@Deprecated("Please migrate to the new form implementation. This legacy code will be removed in a future version.")
typealias FormTriggers = Map<FieldName, FieldValidateOn>
@Deprecated("Please migrate to the new form implementation. This legacy code will be removed in a future version.")
typealias FormRules<T> = Map<FieldName, FormRule<T>>
