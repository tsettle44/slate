package com.slate.phone.policy

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.policyDataStore: DataStore<Preferences> by preferencesDataStore(name = "slate_policy")

@Singleton
class PolicyRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    val policyFlow: Flow<SlatePolicy> = context.policyDataStore.data.map { preferences ->
        preferences[POLICY_KEY]?.let { stored ->
            runCatching { json.decodeFromString<SlatePolicy>(stored) }.getOrDefault(SlatePolicy())
        } ?: SlatePolicy()
    }

    suspend fun getPolicy(): SlatePolicy = policyFlow.first()

    suspend fun savePolicy(policy: SlatePolicy) {
        context.policyDataStore.edit { preferences ->
            preferences[POLICY_KEY] = json.encodeToString(policy)
        }
    }

    suspend fun updatePolicy(transform: (SlatePolicy) -> SlatePolicy) {
        savePolicy(transform(getPolicy()))
    }

    fun isAllowed(packageName: String, policy: SlatePolicy): Boolean {
        if (packageName == context.packageName) return true
        if (policy.blocklistPackages.contains(packageName)) return false
        if (!policy.allowlistPackages.contains(packageName)) return false

        return when (policy.currentMode) {
            SlateMode.SLEEP -> SlatePolicy.SLEEP_ALLOWED_PACKAGES.contains(packageName)
            SlateMode.FOCUS -> {
                if (policy.chrome.disabled && isBrowserPackage(packageName)) {
                    false
                } else {
                    true
                }
            }
            SlateMode.NORMAL,
            SlateMode.REHAB,
            SlateMode.MAINTENANCE,
            -> true
        }
    }

    fun getDelaySeconds(app: SlateApp, policy: SlatePolicy): Int {
        return when (policy.currentMode) {
            SlateMode.FOCUS -> policy.mindfulDelay.focusDelaySeconds
            SlateMode.SLEEP -> 0
            else -> when (app.tier) {
                AppTier.TIER_1 -> policy.mindfulDelay.tier1DelaySeconds
                AppTier.TIER_2 -> policy.mindfulDelay.tier2DelaySeconds
                AppTier.TIER_BLOCKED -> 0
            }
        }
    }

    private fun isBrowserPackage(packageName: String): Boolean {
        return packageName == "com.android.chrome" ||
            packageName == "com.sec.android.app.sbrowser" ||
            packageName == "org.mozilla.firefox"
    }

    companion object {
        private val POLICY_KEY = stringPreferencesKey("slate_policy_json")
    }
}
