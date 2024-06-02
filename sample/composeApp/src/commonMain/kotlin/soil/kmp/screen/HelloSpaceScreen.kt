package soil.kmp.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import soil.playground.space.compose.Counter
import soil.playground.style.withAppTheme
import soil.space.AtomStore
import soil.space.atom
import soil.space.atomScope
import soil.space.compose.AtomRoot
import soil.space.compose.rememberAtomState
import soil.space.compose.rememberAtomValue
import soil.space.compose.rememberViewModelStore


@Composable
fun HelloSpaceScreen(
    navStore: AtomStore
) {
    AtomRoot(
        currentScreen to rememberViewModelStore(),
        navScreen to navStore,
        fallbackScope = { currentScreen }
        // If fallbackScope is set to navScreen, the value of Counter is preserved even if it navigates back and then forward again.
        // fallbackScope = { navScreen }
    ) {
        HelloSpaceContent(
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun HelloSpaceContent(
    modifier: Modifier = Modifier
) = withAppTheme {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CounterSection(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .weight(1f)
                .background(colorScheme.surface)
        )
        CounterSection(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .weight(1f)
                .background(colorScheme.surfaceVariant)
        )
    }
}

@Composable
private fun CounterSection(
    modifier: Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CounterContainer { state ->
            CounterView(
                count = state.count,
                updateCount = state.updateCount,
                counterText = state.counterText
            )
        }
    }
}

// TIPS: Adopting the Presentational-Container Pattern allows for separation of control and presentation.
// https://www.patterns.dev/react/presentational-container-pattern
@Composable
private fun CounterContainer(
    content: @Composable (CounterState) -> Unit
) {
    var counter by rememberAtomState(counterAtom)
    val counterSelector by rememberAtomValue(counterSelectorAtom)
    val state by remember { derivedStateOf { CounterState(counter, { counter = it }, counterSelector) } }
    content(state)
}

private data class CounterState(
    val count: Int,
    val updateCount: (Int) -> Unit,
    val counterText: String,
)

@Composable
private fun CounterView(
    count: Int,
    updateCount: (Int) -> Unit,
    counterText: String
) = withAppTheme {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Counter(
            value = count,
            onDecrement = { updateCount(count - 1) },
            onIncrement = { updateCount(count + 1) }
        )
        Text(
            text = counterText,
            style = typography.bodySmall
        )
    }
}

private val navScreen = atomScope()
private val currentScreen = atomScope()

// Specifying saverKey is optional. It is necessary for restoration on the Android platform.
private val counterAtom = atom(0, saverKey = "counter")
private val counterSelectorAtom = atom {
    val value = get(counterAtom)
    when {
        0 < value -> "positive number"
        0 > value -> "negative number"
        else -> "zero"
    }
}
