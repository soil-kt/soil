// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.internal

interface NetworkOptions {
    val isNetworkError: ((Throwable) -> Boolean)?
}
