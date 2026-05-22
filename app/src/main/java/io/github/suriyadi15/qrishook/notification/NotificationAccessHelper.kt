package io.github.suriyadi15.qrishook.notification

import android.content.ComponentName
import android.content.Context
import android.provider.Settings

object NotificationAccessHelper {
    fun isGranted(context: Context): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners",
        ).orEmpty()
        val componentName = ComponentName(context, QrisNotificationListenerService::class.java)
        return enabledListeners.split(":").any {
            ComponentName.unflattenFromString(it) == componentName
        }
    }
}
