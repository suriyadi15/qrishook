package io.github.suriyadi15.qrishook.webhook

import io.github.suriyadi15.qrishook.data.AppSettings
import io.github.suriyadi15.qrishook.data.EventDeliveryStore
import io.github.suriyadi15.qrishook.data.EventEntity

class WebhookDeliveryRunner(
    private val eventStore: EventDeliveryStore,
    private val webhookSender: WebhookSender,
) {
    suspend fun deliver(event: EventEntity, settings: AppSettings): WebhookDeliveryResult {
        eventStore.markSending(event.eventId)
        return when (val result = webhookSender.send(event, settings)) {
            is WebhookResult.Success -> {
                eventStore.markSent(event.eventId, result)
                WebhookDeliveryResult.Success
            }
            is WebhookResult.Failure -> {
                eventStore.markFailed(event.eventId, result)
                WebhookDeliveryResult.Failure
            }
        }
    }

    suspend fun deliverBatch(
        events: List<EventEntity>,
        settings: AppSettings,
    ): WebhookBatchDeliveryResult {
        var hasFailure = false
        for (event in events) {
            if (deliver(event, settings) is WebhookDeliveryResult.Failure) {
                hasFailure = true
            }
        }
        return if (hasFailure) WebhookBatchDeliveryResult.HasFailure else WebhookBatchDeliveryResult.Success
    }
}

sealed interface WebhookDeliveryResult {
    data object Success : WebhookDeliveryResult
    data object Failure : WebhookDeliveryResult
}

sealed interface WebhookBatchDeliveryResult {
    data object Success : WebhookBatchDeliveryResult
    data object HasFailure : WebhookBatchDeliveryResult
}
