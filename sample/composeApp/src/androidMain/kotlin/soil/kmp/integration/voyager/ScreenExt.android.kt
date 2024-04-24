package soil.kmp.integration.voyager

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import soil.space.AtomStore
import soil.space.compose.rememberViewModelStore

@Composable
actual fun Screen.rememberScreenStore(key: String?): AtomStore {
    return rememberViewModelStore(key)
}
