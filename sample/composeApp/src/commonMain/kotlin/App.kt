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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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

@Composable
private fun Content(
    navController: NavHostController = rememberNavController()
) = withAppTheme {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val navigator = remember(navController) { Navigator(navController) }
    val canNavigateBack = remember(backStackEntry) { navigator.canBack() }
    val hostState = remember { SnackbarHostState() }
    val feedbackAction = remember { FeedbackAction(hostState) }
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppBar(
                canNavigateBack = canNavigateBack,
                navigateUp = { navigator.back() }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState)
        }
    ) { innerPadding ->
        CompositionLocalProvider(LocalFeedbackHost provides feedbackAction) {
            NavRouterHost(
                navigator = navigator,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    canNavigateBack: Boolean,
    navigateUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { },
        modifier = modifier,
        navigationIcon = {
            if (canNavigateBack) {
                IconButton(onClick = navigateUp) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        }
    )
}
