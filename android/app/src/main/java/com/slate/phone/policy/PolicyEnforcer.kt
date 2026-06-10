package com.slate.phone.policy

import android.content.Context
import android.content.pm.PackageManager
import com.slate.phone.admin.DeviceOwnerManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PolicyEnforcer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceOwnerManager: DeviceOwnerManager,
    private val policyRepository: PolicyRepository,
) {
    suspend fun applyCurrentPolicy() {
        val policy = policyRepository.getPolicy()
        applyPolicy(policy)
    }

    suspend fun applyPolicy(policy: SlatePolicy) {
        if (!deviceOwnerManager.isDeviceOwner()) return

        val installedPackages = context.packageManager
            .getInstalledApplications(PackageManager.GET_META_DATA)
            .map { it.packageName }
            .toSet()

        installedPackages.forEach { packageName ->
            if (packageName == context.packageName) return@forEach
            val shouldHide = !policyRepository.isAllowed(packageName, policy) ||
                policy.blocklistPackages.contains(packageName)
            deviceOwnerManager.setApplicationHidden(packageName, shouldHide)
        }
    }
}
