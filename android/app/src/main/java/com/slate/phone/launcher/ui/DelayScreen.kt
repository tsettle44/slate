package com.slate.phone.launcher.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.slate.phone.R
import com.slate.phone.launcher.theme.SlateTypography
import com.slate.phone.launcher.theme.slatePrimaryFont
import com.slate.phone.launcher.theme.slateSecondaryTextColor
import com.slate.phone.policy.SlateApp
import kotlinx.coroutines.delay

@Composable
fun DelayScreen(
    app: SlateApp,
    delaySeconds: Int,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var remaining by remember(app.packageName, delaySeconds) { mutableIntStateOf(delaySeconds) }

    LaunchedEffect(app.packageName, delaySeconds) {
        remaining = delaySeconds
        while (remaining > 0) {
            delay(1_000)
            remaining -= 1
        }
        onComplete()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = remaining.toString(),
                style = SlateTypography.Countdown,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = slatePrimaryFont(),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.opening_app, app.displayName),
                style = SlateTypography.Date,
                color = slateSecondaryTextColor(),
                fontFamily = slatePrimaryFont(),
            )
        }
    }
}
