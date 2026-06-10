package com.slate.phone.launcher

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.slate.phone.launcher.theme.SlateTheme
import com.slate.phone.launcher.theme.SlateThemeMode
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LauncherActivity : ComponentActivity() {
    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        hideSystemBars()

        onBackPressedDispatcher.addCallback(this) {
            // Remain on the launcher; do not exit.
        }

        setContent {
            SlateTheme(themeMode = SlateThemeMode.PAPER_NIGHT) {
                SlateNavHost(
                    viewModel = viewModel,
                    onLaunchIntent = { intent -> startActivity(intent) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onLauncherResumed()
        maybeStartLockTask()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun maybeStartLockTask() {
        val dpm = getSystemService(android.app.admin.DevicePolicyManager::class.java)
        if (dpm.isLockTaskPermitted(packageName)) {
            runCatching { startLockTask() }
        }
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}
