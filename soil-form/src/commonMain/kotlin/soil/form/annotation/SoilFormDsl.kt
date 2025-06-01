// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.form.annotation

@DslMarker
@Target(allowedTargets = [AnnotationTarget.CLASS, AnnotationTarget.TYPE, AnnotationTarget.FUNCTION])
annotation class SoilFormDsl
