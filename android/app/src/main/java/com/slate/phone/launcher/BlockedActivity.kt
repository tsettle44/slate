package com.slate.phone.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.slate.phone.launcher.theme.SlateTheme
import com.slate.phone.launcher.theme.SlateThemeMode
import com.slate.phone.launcher.ui.BlockedOverlay

class BlockedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SlateTheme(themeMode = SlateThemeMode.PAPER_NIGHT) {
                BlockedOverlay(
                    onDismiss = { finish() },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
