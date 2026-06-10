package com.slate.phone.policy

import com.slate.phone.admin.DeviceOwnerManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModeController @Inject constructor(
    private val policyRepository: PolicyRepository,
    private val deviceOwnerManager: DeviceOwnerManager,
    private val policyEnforcer: PolicyEnforcer,
) {
    suspend fun transitionTo(mode: SlateMode, source: String) {
        val current = policyRepository.getPolicy()
        if (current.currentMode == SlateMode.REHAB && mode != SlateMode.MAINTENANCE) {
            requireRehabUnlock(current)
        }

        deviceOwnerManager.applyModeRestrictions(mode)

        val updated = current.copy(
            currentMode = mode,
            previousMode = current.currentMode,
            auditLog = current.auditLog + AuditEntry(
                timestamp = System.currentTimeMillis(),
                action = "mode_change",
                detail = "${current.currentMode} -> $mode",
                source = source,
            ),
        )
        policyRepository.savePolicy(updated)
        policyEnforcer.applyPolicy(updated)
    }

    private fun requireRehabUnlock(policy: SlatePolicy) {
        val now = System.currentTimeMillis()
        if (now < policy.rehabUnlockAvailableAt) {
            val remainingHours = ((policy.rehabUnlockAvailableAt - now) / 3_600_000) + 1
            error("Rehab unlock cooldown active ($remainingHours h remaining)")
        }
    }
}
