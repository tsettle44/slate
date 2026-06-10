package com.slate.phone.policy

import kotlinx.serialization.Serializable

@Serializable
enum class SlateMode {
    NORMAL,
    FOCUS,
    SLEEP,
    REHAB,
    MAINTENANCE,
}

@Serializable
enum class AppTier {
    TIER_1,
    TIER_2,
    TIER_BLOCKED,
}

@Serializable
data class MindfulDelayConfig(
    val tier1DelaySeconds: Int = 0,
    val tier2DelaySeconds: Int = 15,
    val focusDelaySeconds: Int = 30,
    val allowPinSkip: Boolean = false,
)

@Serializable
data class SleepSchedule(
    val enabled: Boolean = true,
    val startMinutes: Int = 22 * 60,
    val endMinutes: Int = 7 * 60,
)

@Serializable
data class NotificationConfig(
    val allowedPackages: List<String> = emptyList(),
    val suppressLockScreen: Boolean = true,
)

@Serializable
data class ChromePolicy(
    val disabled: Boolean = false,
    val blockedDomains: List<String> = SlatePolicy.DEFAULT_BLOCKED_DOMAINS,
)

@Serializable
data class AuditEntry(
    val timestamp: Long,
    val action: String,
    val detail: String,
    val source: String,
)

@Serializable
data class SlatePolicy(
    val version: Int = 1,
    val currentMode: SlateMode = SlateMode.NORMAL,
    val previousMode: SlateMode = SlateMode.NORMAL,
    val allowlistPackages: List<String> = DEFAULT_ALLOWLIST,
    val blocklistPackages: List<String> = DEFAULT_BLOCKLIST,
    val appTiers: Map<String, AppTier> = DEFAULT_APP_TIERS,
    val mindfulDelay: MindfulDelayConfig = MindfulDelayConfig(),
    val sleepSchedule: SleepSchedule = SleepSchedule(),
    val notifications: NotificationConfig = NotificationConfig(
        allowedPackages = DEFAULT_NOTIFICATION_ALLOWLIST,
    ),
    val chrome: ChromePolicy = ChromePolicy(),
    val maintenanceExpiresAt: Long = 0L,
    val rehabUnlockAvailableAt: Long = 0L,
    val auditLog: List<AuditEntry> = emptyList(),
) {
    companion object {
        val DEFAULT_ALLOWLIST = listOf(
            "com.samsung.android.dialer",
            "com.google.android.dialer",
            "com.samsung.android.messaging",
            "com.google.android.apps.messaging",
            "com.google.android.apps.maps",
            "com.spotify.music",
            "com.sec.android.app.camera",
            "com.android.chrome",
            "com.google.android.calendar",
        )

        val DEFAULT_BLOCKLIST = listOf(
            "com.android.vending",
            "com.sec.android.app.samsungapps",
            "com.google.android.youtube",
            "com.instagram.android",
            "com.zhiliaoapp.musically",
            "com.twitter.android",
            "com.reddit.frontpage",
            "com.facebook.katana",
            "com.snapchat.android",
            "com.samsung.android.app.spage",
        )

        val DEFAULT_NOTIFICATION_ALLOWLIST = listOf(
            "com.samsung.android.dialer",
            "com.google.android.dialer",
            "com.samsung.android.messaging",
            "com.google.android.apps.messaging",
            "com.google.android.calendar",
        )

        val DEFAULT_APP_TIERS = mapOf(
            "com.samsung.android.dialer" to AppTier.TIER_1,
            "com.google.android.dialer" to AppTier.TIER_1,
            "com.samsung.android.messaging" to AppTier.TIER_1,
            "com.google.android.apps.messaging" to AppTier.TIER_1,
            "com.google.android.apps.maps" to AppTier.TIER_1,
            "com.spotify.music" to AppTier.TIER_1,
            "com.sec.android.app.camera" to AppTier.TIER_1,
            "com.android.chrome" to AppTier.TIER_2,
            "com.google.android.calendar" to AppTier.TIER_2,
        )

        val DEFAULT_BLOCKED_DOMAINS = listOf(
            "instagram.com",
            "tiktok.com",
            "twitter.com",
            "x.com",
            "reddit.com",
            "facebook.com",
            "youtube.com",
        )

        val SLEEP_ALLOWED_PACKAGES = setOf(
            "com.samsung.android.dialer",
            "com.google.android.dialer",
            "com.slate.phone",
        )
    }
}

data class SlateApp(
    val packageName: String,
    val displayName: String,
    val tier: AppTier,
    val launchIntent: android.content.Intent,
)
