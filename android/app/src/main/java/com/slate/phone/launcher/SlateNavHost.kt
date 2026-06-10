package com.slate.phone.launcher

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.slate.phone.launcher.ui.BlockedOverlay
import com.slate.phone.launcher.ui.DelayScreen
import com.slate.phone.launcher.ui.DevModeBanner
import com.slate.phone.launcher.ui.HomeScreen
import com.slate.phone.launcher.ui.SearchScreen
import com.slate.phone.policy.SlateApp
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

private object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val DELAY = "delay/{packageName}/{delaySeconds}"
    const val BLOCKED = "blocked"

    fun delay(packageName: String, delaySeconds: Int) =
        "delay/$packageName/$delaySeconds"
}

@Composable
fun SlateNavHost(
    viewModel: LauncherViewModel,
    onLaunchIntent: (android.content.Intent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is LauncherEvent.LaunchApp -> onLaunchIntent(event.intent)
                is LauncherEvent.ShowBlocked -> navController.navigate(Routes.BLOCKED)
                is LauncherEvent.NavigateToDelay -> navController.navigate(
                    Routes.delay(event.app.packageName, event.delaySeconds),
                )
            }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        if (uiState.showDevBanner) {
            DevModeBanner()
        }

        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    state = uiState,
                    onAppClick = viewModel::onAppSelected,
                    onSearchClick = { navController.navigate(Routes.SEARCH) },
                )
            }

            composable(Routes.SEARCH) {
                SearchScreen(
                    apps = uiState.allApps,
                    onAppClick = viewModel::onAppSelected,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(
                route = Routes.DELAY,
                arguments = listOf(
                    navArgument("packageName") { type = NavType.StringType },
                    navArgument("delaySeconds") { type = NavType.IntType },
                ),
            ) { entry ->
                val packageName = URLDecoder.decode(
                    entry.arguments?.getString("packageName").orEmpty(),
                    StandardCharsets.UTF_8.toString(),
                )
                val delaySeconds = entry.arguments?.getInt("delaySeconds") ?: 0
                val app = uiState.allApps.find { it.packageName == packageName }
                    ?: uiState.tier1Apps.find { it.packageName == packageName }

                if (app == null) {
                    LaunchedEffect(packageName) { navController.popBackStack() }
                } else {
                    DelayScreen(
                        app = app,
                        delaySeconds = delaySeconds,
                        onComplete = {
                            viewModel.onDelayComplete(app)
                            navController.popBackStack()
                        },
                    )
                }
            }

            composable(Routes.BLOCKED) {
                BlockedOverlay(
                    onDismiss = { navController.popBackStack() },
                )
            }
        }
    }
}
