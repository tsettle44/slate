package com.slate.phone.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.slate.phone.di.BootReceiverEntryPoint
import com.slate.phone.launcher.LauncherActivity
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        scope.launch {
            try {
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    BootReceiverEntryPoint::class.java,
                )
                entryPoint.policyEnforcer().applyCurrentPolicy()
            } finally {
                pendingResult.finish()
            }
        }

        val launcherIntent = Intent(context, LauncherActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(launcherIntent)
    }
}
