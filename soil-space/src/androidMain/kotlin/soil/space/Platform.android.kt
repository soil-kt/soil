// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.space

import android.os.Bundle
import android.os.Parcelable
import androidx.savedstate.SavedStateRegistry
import java.io.Serializable

actual typealias CommonParcelable = Parcelable
actual typealias CommonSerializable = Serializable
actual typealias CommonBundle = Bundle
actual typealias CommonSavedStateProvider = SavedStateRegistry.SavedStateProvider
