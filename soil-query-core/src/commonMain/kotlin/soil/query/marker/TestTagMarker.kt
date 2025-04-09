// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.marker

import soil.query.core.Marker
import soil.query.core.TestTag

/**
 * Extension function to add a TestTag to a Marker.
 * 
 * This function is used to attach a TestTag to a Marker, which can then be passed to query, mutation,
 * or subscription operations to identify them for mocking in test and preview environments.
 * 
 * Usage example:
 * ```kotlin
 * val testTag = MyQueryTestTag()
 * val marker = Marker.testTag(testTag)
 * val query = client.getQuery(queryKey, marker)
 * ```
 * 
 * @param value The TestTag to attach to the Marker
 * @return A new Marker with the TestTag attached
 */
fun Marker.testTag(value: TestTag): Marker {
    return this + TestTagMarker(value)
}

/**
 * Marker element that holds a TestTag for identification in test and preview environments.
 * 
 * This marker element is used internally by the testTag extension function to attach a TestTag
 * to a Marker that will be passed to query, mutation, or subscription operations.
 * 
 * The TestTagMarker allows the client implementation to identify the operation by its
 * TestTag rather than its ID, which is particularly useful when dealing with auto-generated
 * IDs that might change during configuration changes or recompositions.
 */
class TestTagMarker internal constructor(val value: TestTag) : Marker.Element {
    override val key: Marker.Key<*>
        get() = Key

    companion object Key : Marker.Key<TestTagMarker>
}
