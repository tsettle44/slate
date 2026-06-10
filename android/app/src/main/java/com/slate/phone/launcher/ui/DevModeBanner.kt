package com.slate.phone.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.slate.phone.R
import com.slate.phone.launcher.theme.SlateColors
import com.slate.phone.launcher.theme.SlateTypography

@Composable
fun DevModeBanner(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(SlateColors.Accent.copy(alpha = 0.2f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.dev_mode_banner),
            style = SlateTypography.Date,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
