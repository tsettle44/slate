package com.slate.phone.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.slate.phone.policy.AppTier
import com.slate.phone.policy.SlateApp
import com.slate.phone.policy.SlatePolicy
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppResolver @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun resolveAllowlistedApps(policy: SlatePolicy): List<SlateApp> {
        val packageManager = context.packageManager
        return policy.allowlistPackages
            .mapNotNull { packageName -> resolveLaunchableApp(packageManager, packageName, policy) }
            .sortedBy { it.displayName.lowercase() }
    }

    fun tier1Apps(policy: SlatePolicy): List<SlateApp> {
        return resolveAllowlistedApps(policy).filter { it.tier == AppTier.TIER_1 }
    }

    fun tier2Apps(policy: SlatePolicy): List<SlateApp> {
        return resolveAllowlistedApps(policy).filter { it.tier == AppTier.TIER_2 }
    }

    private fun resolveLaunchableApp(
        packageManager: PackageManager,
        packageName: String,
        policy: SlatePolicy,
    ): SlateApp? {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: return null
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val tier = policy.appTiers[packageName] ?: AppTier.TIER_2
        if (tier == AppTier.TIER_BLOCKED) return null

        val label = runCatching {
            packageManager.getApplicationLabel(
                packageManager.getApplicationInfo(packageName, 0),
            ).toString()
        }.getOrDefault(packageName)

        return SlateApp(
            packageName = packageName,
            displayName = label,
            tier = tier,
            launchIntent = launchIntent,
        )
    }
}
