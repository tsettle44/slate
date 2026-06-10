package com.slate.phone.admin

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.slate.phone.R

class SlateDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        Toast.makeText(context, R.string.app_name, Toast.LENGTH_SHORT).show()
    }
}
