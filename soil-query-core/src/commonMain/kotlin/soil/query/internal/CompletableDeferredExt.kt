// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.internal

import kotlinx.coroutines.CompletableDeferred

internal fun <T> CompletableDeferred<T>.toResultCallback(): (Result<T>) -> Unit {
    return { result ->
        result
            .onSuccess { complete(it) }
            .onFailure { completeExceptionally(it) }
    }
}
