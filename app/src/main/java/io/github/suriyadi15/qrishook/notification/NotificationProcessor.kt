package io.github.suriyadi15.qrishook.notification

import android.app.Notification
import android.content.Context
import android.content.pm.PackageManager
import android.service.notification.StatusBarNotification
import android.util.Log
import io.github.suriyadi15.qrishook.AppGraph
import io.github.suriyadi15.qrishook.domain.ObservedNotification
import io.github.suriyadi15.qrishook.merchant.MerchantRegistry
import io.github.suriyadi15.qrishook.webhook.WebhookDeliveryResult
import io.github.suriyadi15.qrishook.worker.WebhookDeliveryWorker
import kotlinx.coroutines.flow.first

class NotificationProcessor(
    private val context: Context,
    private val packageManager: PackageManager,
) {
    suspend fun process(sbn: StatusBarNotification) {
        runCatching {
            val graph = AppGraph.get(context)
            val settings = graph.settingsRepository.settings.first()

            if (!settings.qrisHookActive) return

            if (settings.debugModeEnabled) {
                if (sbn.packageName in settings.debugWatchedPackages) {
                    graph.debugNotificationRepository.insert(
                        DebugNotificationPayloadBuilder.build(sbn, packageManager),
                    )
                }
                return
            }

            val observed = sbn.toObservedNotification()
            val parsers = MerchantRegistry.selectedParsers(settings.selectedMerchantIds)
            val events = graph.matcher.match(observed, parsers)
            if (events.isEmpty()) return

            var hasDeliveryFailure = false
            events.forEach { event ->
                val queuedEvent = graph.eventRepository.enqueue(event)
                runCatching {
                    graph.webhookDeliveryRunner.deliver(queuedEvent, settings)
                }.onSuccess { deliveryResult ->
                    if (deliveryResult is WebhookDeliveryResult.Failure) {
                        hasDeliveryFailure = true
                    }
                }.onFailure { error ->
                    hasDeliveryFailure = true
                    Log.e(TAG, "Failed to deliver queued event ${queuedEvent.eventId}", error)
                }
            }

            if (hasDeliveryFailure) {
                WebhookDeliveryWorker.enqueue(context)
            }
        }.onFailure { error ->
            Log.e(TAG, "Failed to process notification from ${sbn.packageName}", error)
        }
    }

    private fun StatusBarNotification.toObservedNotification(): ObservedNotification {
        val extras = notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val appName = runCatching {
            val info = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(info).toString()
        }.getOrDefault(packageName)

        return ObservedNotification(
            sourcePackage = packageName,
            sourceApp = appName,
            title = title,
            text = text,
            bigText = bigText,
            postedAtMillis = postTime,
        )
    }

    private companion object {
        const val TAG = "QrisNotification"
    }
}
