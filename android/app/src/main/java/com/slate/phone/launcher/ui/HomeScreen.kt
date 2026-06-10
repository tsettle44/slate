package com.slate.phone.launcher.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.slate.phone.R
import com.slate.phone.launcher.LauncherUiState
import com.slate.phone.launcher.theme.SlateTypography
import com.slate.phone.launcher.theme.slatePrimaryFont
import com.slate.phone.launcher.theme.slateSecondaryTextColor
import com.slate.phone.policy.SlateApp
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    state: LauncherUiState,
    onAppClick: (SlateApp) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val now = state.currentTime
    val timeFormatter = DateTimeFormatter.ofPattern("H:mm", Locale.getDefault())
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.getDefault())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 48.dp),
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = timeFormatter.format(now),
            style = SlateTypography.Clock,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = slatePrimaryFont(),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = dateFormatter.format(now),
            style = SlateTypography.Date,
            color = slateSecondaryTextColor(),
            fontFamily = slatePrimaryFont(),
        )

        Spacer(modifier = Modifier.height(56.dp))

        if (state.tier1Apps.isEmpty()) {
            Text(
                text = stringResource(R.string.no_apps),
                style = SlateTypography.AppName,
                color = slateSecondaryTextColor(),
            )
        } else {
            state.tier1Apps.forEach { app ->
                AppRow(app = app, onClick = { onAppClick(app) })
                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onSearchClick)
                .padding(vertical = 8.dp),
        ) {
            Text(
                text = stringResource(R.string.search),
                style = SlateTypography.AppName,
                color = slateSecondaryTextColor(),
                fontFamily = slatePrimaryFont(),
                modifier = Modifier.align(Alignment.CenterStart),
            )
        }
    }
}

@Composable
private fun AppRow(
    app: SlateApp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = app.displayName,
        style = SlateTypography.AppName,
        color = MaterialTheme.colorScheme.onBackground,
        fontFamily = slatePrimaryFont(),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
    )
}

fun currentZonedDateTime(): ZonedDateTime = ZonedDateTime.now()
