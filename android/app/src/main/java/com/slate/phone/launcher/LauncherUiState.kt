package com.slate.phone.launcher

import com.slate.phone.launcher.ui.currentZonedDateTime
import com.slate.phone.policy.SlateApp
import com.slate.phone.policy.SlateMode
import com.slate.phone.policy.SlatePolicy
import java.time.ZonedDateTime

data class LauncherUiState(
    val policy: SlatePolicy = SlatePolicy(),
    val tier1Apps: List<SlateApp> = emptyList(),
    val allApps: List<SlateApp> = emptyList(),
    val isDeviceOwner: Boolean = false,
    val currentTime: ZonedDateTime = currentZonedDateTime(),
) {
    val showDevBanner: Boolean get() = !isDeviceOwner
    val currentMode: SlateMode get() = policy.currentMode
}
