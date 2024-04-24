// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

// NOTE: Extension receiver for referencing external instances needed when executing mutate
interface MutationReceiver {
    companion object : MutationReceiver
}
