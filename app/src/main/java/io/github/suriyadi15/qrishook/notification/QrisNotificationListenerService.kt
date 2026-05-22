package io.github.suriyadi15.qrishook.notification

import android.content.ComponentName
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import io.github.suriyadi15.qrishook.AppGraph
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class QrisNotificationListenerService : NotificationListenerService() {
    private val processor by lazy {
        NotificationProcessor(applicationContext, packageManager)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        AppGraph.get(applicationContext).applicationScope.launch {
            processor.process(sbn)
        }
    }

    override fun onListenerConnected() {
        AppGraph.get(applicationContext).applicationScope.launch {
            val settings = AppGraph.get(applicationContext).settingsRepository.settings.first()
            if (settings.qrisHookActive) {
                WatcherForegroundService.start(applicationContext)
            } else {
                WatcherForegroundService.stop(applicationContext)
            }
        }
        super.onListenerConnected()
    }

    override fun onListenerDisconnected() {
        NotificationListenerService.requestRebind(
            ComponentName(this, QrisNotificationListenerService::class.java),
        )
        super.onListenerDisconnected()
    }
}
