import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import soil.kmp.screen.HomeScreen
import soil.playground.FeedbackAction
import soil.playground.LocalFeedbackHost
import soil.playground.style.AppTheme
import soil.playground.style.withAppTheme


@Composable
fun App() {
    AppTheme {
        Content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content() = withAppTheme {
    Navigator(HomeScreen) { navigator ->
        val hostState = remember { SnackbarHostState() }
        val feedbackAction = remember { FeedbackAction(hostState) }
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (navigator.canPop) {
                    TopAppBar(
                        title = { },
                        navigationIcon = {
                            IconButton(onClick = navigator::pop) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(hostState)
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                CompositionLocalProvider(LocalFeedbackHost provides feedbackAction) {
                    CurrentScreen()
                }
            }
        }
    }
}
