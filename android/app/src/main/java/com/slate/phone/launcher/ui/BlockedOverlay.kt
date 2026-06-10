package com.slate.phone.launcher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.slate.phone.R
import com.slate.phone.launcher.theme.SlateTypography
import com.slate.phone.launcher.theme.slatePrimaryFont
import com.slate.phone.launcher.theme.slateSecondaryTextColor

@Composable
fun BlockedOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.not_available),
            style = SlateTypography.AppName,
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = slatePrimaryFont(),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.not_available_message),
            style = SlateTypography.Date,
            color = slateSecondaryTextColor(),
            fontFamily = slatePrimaryFont(),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
            Text(text = stringResource(R.string.ok))
        }
    }
}
