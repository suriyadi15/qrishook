package io.github.suriyadi15.qrishook.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.github.suriyadi15.qrishook.AppGraph
import io.github.suriyadi15.qrishook.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class WatcherForegroundService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createChannel()
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST,
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scope.launch {
            val settings = AppGraph.get(applicationContext).settingsRepository.settings.first()
            if (!settings.qrisHookActive) {
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("QRIS Hook is active")
            .setContentText("QRIS Hook is monitoring notifications.")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "QRIS Hook Monitoring",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "QRIS Hook background monitoring status"
        }

        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "qris_hook_watcher"
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context.applicationContext, WatcherForegroundService::class.java)
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.applicationContext.startForegroundService(intent)
                } else {
                    context.applicationContext.startService(intent)
                }
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context.applicationContext, WatcherForegroundService::class.java)
            context.applicationContext.stopService(intent)
        }
    }
}
