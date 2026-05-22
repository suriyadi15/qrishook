package io.github.suriyadi15.qrishook.notification

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.service.notification.NotificationListenerService
import io.github.suriyadi15.qrishook.AppGraph
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WatcherRestoreReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in RESTORE_ACTIONS) return

        val pendingResult = goAsync()
        val appContext = context.applicationContext
        AppGraph.get(appContext).applicationScope.launch {
            runCatching {
                val settings = AppGraph.get(appContext).settingsRepository.settings.first()
                if (settings.qrisHookActive && NotificationAccessHelper.isGranted(appContext)) {
                    NotificationListenerService.requestRebind(
                        ComponentName(appContext, QrisNotificationListenerService::class.java),
                    )
                    WatcherForegroundService.start(appContext)
                } else {
                    WatcherForegroundService.stop(appContext)
                }
            }
            pendingResult.finish()
        }
    }

    private companion object {
        val RESTORE_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
        )
    }
}
