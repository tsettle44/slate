package com.slate.phone.launcher.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.slate.phone.launcher.theme.SlateTypography
import com.slate.phone.launcher.theme.slatePrimaryFont
import com.slate.phone.launcher.theme.slateSecondaryTextColor
import com.slate.phone.policy.SlateApp

@Composable
fun SearchScreen(
    apps: List<SlateApp>,
    onAppClick: (SlateApp) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = apps.filter {
        it.displayName.contains(query, ignoreCase = true) ||
            it.packageName.contains(query, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp, vertical = 48.dp),
    ) {
        TextButton(onClick = onBack) {
            Text(
                text = "Back",
                color = slateSecondaryTextColor(),
            )
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Search apps",
                    color = slateSecondaryTextColor(),
                )
            },
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filtered, key = { it.packageName }) { app ->
                Text(
                    text = app.displayName,
                    style = SlateTypography.AppName,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontFamily = slatePrimaryFont(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAppClick(app) }
                        .padding(vertical = 12.dp),
                )
            }
        }
    }
}
