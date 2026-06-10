package com.slate.phone.admin

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.UserManager
import com.slate.phone.launcher.LauncherActivity
import com.slate.phone.policy.SlateMode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceOwnerManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val devicePolicyManager: DevicePolicyManager =
        context.getSystemService(DevicePolicyManager::class.java)

    val adminComponent: ComponentName =
        ComponentName(context, SlateDeviceAdminReceiver::class.java)

    fun isDeviceOwner(): Boolean =
        devicePolicyManager.isDeviceOwnerApp(context.packageName)

    fun isDeviceAdmin(): Boolean =
        devicePolicyManager.isAdminActive(adminComponent)

    fun initializeDeviceOwner() {
        if (!isDeviceOwner()) return

        devicePolicyManager.setLockTaskPackages(adminComponent, arrayOf(context.packageName))
        devicePolicyManager.setLockTaskFeatures(
            adminComponent,
            DevicePolicyManager.LOCK_TASK_FEATURE_NONE,
        )

        val homeFilter = IntentFilter(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addCategory(Intent.CATEGORY_DEFAULT)
        }
        devicePolicyManager.addPersistentPreferredActivity(
            adminComponent,
            homeFilter,
            ComponentName(context, LauncherActivity::class.java),
        )

        applyModeRestrictions(SlateMode.NORMAL)
    }

    fun applyModeRestrictions(mode: SlateMode) {
        if (!isDeviceOwner()) return

        val restrictions = restrictionSetFor(mode)
        ALL_RESTRICTIONS.forEach { restriction ->
            if (restrictions.contains(restriction)) {
                devicePolicyManager.addUserRestriction(adminComponent, restriction)
            } else {
                devicePolicyManager.clearUserRestriction(adminComponent, restriction)
            }
        }
    }

    fun setApplicationHidden(packageName: String, hidden: Boolean) {
        if (!isDeviceOwner()) return
        devicePolicyManager.setApplicationHidden(adminComponent, packageName, hidden)
    }

    private fun restrictionSetFor(mode: SlateMode): Set<String> = when (mode) {
        SlateMode.MAINTENANCE -> setOf(UserManager.DISALLOW_ADD_USER)
        SlateMode.REHAB -> setOf(
            UserManager.DISALLOW_INSTALL_APPS,
            UserManager.DISALLOW_UNINSTALL_APPS,
            UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES,
            UserManager.DISALLOW_SAFE_BOOT,
            UserManager.DISALLOW_FACTORY_RESET,
            UserManager.DISALLOW_ADD_USER,
            UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA,
        )
        SlateMode.NORMAL,
        SlateMode.FOCUS,
        SlateMode.SLEEP,
        -> setOf(
            UserManager.DISALLOW_INSTALL_APPS,
            UserManager.DISALLOW_UNINSTALL_APPS,
            UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES,
            UserManager.DISALLOW_ADD_USER,
        )
    }

    companion object {
        private val ALL_RESTRICTIONS = setOf(
            UserManager.DISALLOW_INSTALL_APPS,
            UserManager.DISALLOW_UNINSTALL_APPS,
            UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES,
            UserManager.DISALLOW_SAFE_BOOT,
            UserManager.DISALLOW_FACTORY_RESET,
            UserManager.DISALLOW_ADD_USER,
            UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA,
        )
    }
}
