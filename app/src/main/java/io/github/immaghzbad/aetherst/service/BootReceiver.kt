package io.github.immaghzbad.aetherst.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.immaghzbad.aetherst.core.AutoConnectManager
import io.github.immaghzbad.aetherst.shared.data.LogRepository

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON",
            "com.htc.intent.action.QUICKBOOT_POWERON" -> {
                LogRepository.i("[BootReceiver] Boot completed, checking auto-connect")
                AutoConnectManager.handleBootCompleted(context)
            }
        }
    }
}
