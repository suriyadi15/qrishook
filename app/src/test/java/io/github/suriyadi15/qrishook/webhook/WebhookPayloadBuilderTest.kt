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
        assertTrue(payload.contains("\"merchant_id\":\"mandiri_merchant\""))
        assertTrue(payload.contains("\"source_package\":\"com.bankmandiri.merchant\""))
        assertTrue(payload.contains("\"source_app\":\"Mandiri Merchant\""))
        assertFalse(payload.contains("\"received_at\":\"2026-05-21T12:00:00Z\",\"title\""))
        assertFalse(payload.contains("\"received_at\":\"2026-05-21T12:00:00Z\",\"text\""))
        assertFalse(payload.contains("\"received_at\":\"2026-05-21T12:00:00Z\",\"amount\""))
        assertFalse(payload.contains("\"received_at\":\"2026-05-21T12:00:00Z\",\"currency\""))
        assertTrue(
            payload.contains(
                "\"notification\":{" +
                    "\"source_package\":\"com.bankmandiri.merchant\"," +
                    "\"source_app\":\"Mandiri Merchant\"," +
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
                    "\"sender_name\":\"Jhon Doe\"," +
                    "\"payment_source\":\"Mandiri\"" +
                    "}",
            ),
        )
        assertTrue(payload.contains("\"received_at\":\"2026-05-21T12:00:00Z\""))
        assertTrue(
            payload.contains(
                "\"raw\":{" +
                    "\"source_package\":\"com.bankmandiri.merchant\"," +
                    "\"source_app\":\"Mandiri Merchant\"," +
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
        merchantId = "mandiri_merchant",
        sourcePackage = "com.bankmandiri.merchant",
        sourceApp = "Mandiri Merchant",
        title = "Pembayaran QRIS berhasil",
        text = "QRIS dibayar Rp10.000",
        bigText = "Detail transaksi",
        amount = 10_000L,
        currency = "IDR",
        senderName = "Jhon Doe",
        paymentSource = "Mandiri",
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
