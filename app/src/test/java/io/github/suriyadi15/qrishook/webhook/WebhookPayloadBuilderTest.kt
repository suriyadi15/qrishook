package io.github.suriyadi15.qrishook.webhook

import io.github.suriyadi15.qrishook.data.EventEntity
import io.github.suriyadi15.qrishook.domain.DeliveryStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebhookPayloadBuilderTest {
    @Test
    fun buildsFormattedPayloadWithRawNotificationData() {
        val payload = WebhookPayloadBuilder.build(event())

        assertTrue(payload.contains("\"event_id\":\"event-1\""))
        assertTrue(payload.contains("\"type\":\"qris.payment.success\""))
        assertTrue(payload.contains("\"merchant_id\":\"sample_merchant\""))
        assertTrue(payload.contains("\"source_package\":\"com.example.merchant\""))
        assertTrue(payload.contains("\"source_app\":\"Sample Merchant\""))
        assertFalse(payload.contains("\"received_at\":\"2026-05-21T12:00:00Z\",\"title\""))
        assertFalse(payload.contains("\"received_at\":\"2026-05-21T12:00:00Z\",\"text\""))
        assertFalse(payload.contains("\"received_at\":\"2026-05-21T12:00:00Z\",\"amount\""))
        assertFalse(payload.contains("\"received_at\":\"2026-05-21T12:00:00Z\",\"currency\""))
        assertTrue(
            payload.contains(
                "\"notification\":{" +
                    "\"source_package\":\"com.example.merchant\"," +
                    "\"source_app\":\"Sample Merchant\"," +
                    "\"title\":\"Pembayaran QRIS berhasil\"," +
                    "\"text\":\"QRIS dibayar Rp10.000\"," +
                    "\"big_text\":\"Detail transaksi\"," +
                    "\"received_at\":\"2026-05-21T12:00:00Z\"" +
                    "}",
            ),
        )
        assertTrue(
            payload.contains(
                "\"payment\":{" +
                    "\"amount\":10000," +
                    "\"currency\":\"IDR\"," +
                    "\"sender_name\":\"Sample Sender\"," +
                    "\"payment_source\":\"Sample Source\"" +
                    "}",
            ),
        )
        assertTrue(payload.contains("\"received_at\":\"2026-05-21T12:00:00Z\""))
        assertTrue(
            payload.contains(
                "\"raw\":{" +
                    "\"source_package\":\"com.example.merchant\"," +
                    "\"source_app\":\"Sample Merchant\"," +
                    "\"title\":\"Pembayaran QRIS berhasil\"," +
                    "\"text\":\"QRIS dibayar Rp10.000\"," +
                    "\"big_text\":\"Detail transaksi\"," +
                    "\"received_at\":\"2026-05-21T12:00:00Z\"" +
                    "}",
            ),
        )
        assertTrue(payload.contains("\"big_text\":\"Detail transaksi\""))
        assertFalse(payload.contains("\"raw\":true"))
        assertFalse(payload.contains("\"raw_notification\""))
    }

    private fun event() = EventEntity(
        eventId = "event-1",
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
