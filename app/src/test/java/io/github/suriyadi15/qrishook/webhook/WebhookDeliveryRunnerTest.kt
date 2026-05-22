package io.github.suriyadi15.qrishook.webhook

import io.github.suriyadi15.qrishook.data.AppSettings
import io.github.suriyadi15.qrishook.data.EventDeliveryStore
import io.github.suriyadi15.qrishook.data.EventEntity
import io.github.suriyadi15.qrishook.domain.DeliveryStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WebhookDeliveryRunnerTest {
    @Test
    fun marksEventSentWhenWebhookSucceeds() = runBlocking {
        val store = FakeEventStore()
        val sender = FakeWebhookSender(WebhookResult.Success(200, "OK", "{}"))
        val runner = WebhookDeliveryRunner(store, sender)

        val result = runner.deliver(event(), AppSettings(webhookUrl = "https://example.test/webhook"))

        assertTrue(result is WebhookDeliveryResult.Success)
        assertEquals(listOf("sending:event-1", "sent:event-1:200"), store.calls)
        assertEquals(listOf("event-1"), sender.sentEventIds)
    }

    @Test
    fun marksEventFailedWhenWebhookFails() = runBlocking {
        val store = FakeEventStore()
        val sender = FakeWebhookSender(
            WebhookResult.Failure(
                reason = "HTTP 500",
                code = 500,
                message = "Server Error",
                body = "failed",
            ),
        )
        val runner = WebhookDeliveryRunner(store, sender)

        val result = runner.deliver(event(), AppSettings(webhookUrl = "https://example.test/webhook"))

        assertTrue(result is WebhookDeliveryResult.Failure)
        assertEquals(listOf("sending:event-1", "failed:event-1:HTTP 500"), store.calls)
        assertEquals(listOf("event-1"), sender.sentEventIds)
    }

    @Test
    fun batchReportsFailureWhenAnyEventFails() = runBlocking {
        val store = FakeEventStore()
        val sender = SequenceWebhookSender(
            listOf(
                WebhookResult.Success(200, "OK", "{}"),
                WebhookResult.Failure("timeout", null, "SocketTimeoutException", ""),
            ),
        )
        val runner = WebhookDeliveryRunner(store, sender)

        val result = runner.deliverBatch(
            listOf(event("event-1"), event("event-2")),
            AppSettings(webhookUrl = "https://example.test/webhook"),
        )

        assertTrue(result is WebhookBatchDeliveryResult.HasFailure)
        assertEquals(
            listOf(
                "sending:event-1",
                "sent:event-1:200",
                "sending:event-2",
                "failed:event-2:timeout",
            ),
            store.calls,
        )
    }

    private class FakeEventStore : EventDeliveryStore {
        val calls = mutableListOf<String>()

        override suspend fun markSending(eventId: String) {
            calls += "sending:$eventId"
        }

        override suspend fun markSent(eventId: String, result: WebhookResult.Success) {
            calls += "sent:$eventId:${result.code}"
        }

        override suspend fun markFailed(eventId: String, result: WebhookResult.Failure) {
            calls += "failed:$eventId:${result.reason}"
        }
    }

    private class FakeWebhookSender(
        private val result: WebhookResult,
    ) : WebhookSender {
        val sentEventIds = mutableListOf<String>()

        override fun send(event: EventEntity, settings: AppSettings): WebhookResult {
            sentEventIds += event.eventId
            return result
        }
    }

    private class SequenceWebhookSender(
        results: List<WebhookResult>,
    ) : WebhookSender {
        private val results = ArrayDeque(results)

        override fun send(event: EventEntity, settings: AppSettings): WebhookResult {
            return results.removeFirst()
        }
    }

    private fun event(eventId: String = "event-1") = EventEntity(
        eventId = eventId,
        type = "qris.payment.success",
        merchantId = "sample_merchant",
        sourcePackage = "com.example.merchant",
        sourceApp = "Sample Merchant",
        title = "Pembayaran QRIS berhasil",
        text = "QRIS dibayar Rp10.000",
        bigText = "Detail transaksi",
        amount = 10_000L,
        currency = "IDR",
        senderName = "Sample Sender",
        paymentSource = "Sample Source",
        receivedAt = "2026-05-21T12:00:00Z",
        status = DeliveryStatus.Pending,
        attempts = 0,
        lastError = "",
        lastResponseCode = null,
        lastResponseMessage = "",
        lastResponseBody = "",
        lastWebhookAttemptAtMillis = null,
        createdAtMillis = 1L,
        updatedAtMillis = 1L,
    )
}
