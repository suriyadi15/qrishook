package io.github.suriyadi15.qrishook.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import io.github.suriyadi15.qrishook.AppGraph
import io.github.suriyadi15.qrishook.webhook.WebhookBatchDeliveryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class WebhookDeliveryWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val graph = AppGraph.get(applicationContext)
        val settings = graph.settingsRepository.settings.first()
        val pendingEvents = graph.eventRepository.pendingBatch()

        if (pendingEvents.isEmpty()) return@withContext Result.success()

        when (graph.webhookDeliveryRunner.deliverBatch(pendingEvents, settings)) {
            WebhookBatchDeliveryResult.Success -> Result.success()
            WebhookBatchDeliveryResult.HasFailure -> Result.retry()
        }
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "qris_webhook_delivery"

        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<WebhookDeliveryWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30,
                    TimeUnit.SECONDS,
                )
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }
    }
}
