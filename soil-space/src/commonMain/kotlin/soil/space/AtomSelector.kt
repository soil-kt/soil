// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.space

import androidx.compose.runtime.Immutable

interface AtomSelector {
    fun <T> get(atom: Atom<T>): T
    fun <T> get(atom: AtomRef<T>): T
}

@Immutable
class AtomRef<T>(
    val block: AtomSelector.() -> T
)

fun <T> atom(block: AtomSelector.() -> T): AtomRef<T> {
    return AtomRef(block)
}
