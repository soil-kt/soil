package soil.playground.style

import androidx.compose.material3.MaterialTheme

typealias AppTheme = MaterialTheme

inline fun withAppTheme(block: AppTheme.() -> Unit) {
    with(AppTheme, block)
}
