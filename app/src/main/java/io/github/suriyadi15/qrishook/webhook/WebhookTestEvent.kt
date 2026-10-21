package io.github.suriyadi15.qrishook.webhook

import io.github.suriyadi15.qrishook.data.EventEntity
import io.github.suriyadi15.qrishook.domain.DeliveryStatus
import java.time.Instant
import java.util.UUID

/** Sample event sent by "Send test"; never stored in the database. */
object WebhookTestEvent {
    const val TYPE = "qris.webhook.test"
    const val MERCHANT_ID = "test"

    fun create(now: Instant = Instant.now()): EventEntity {
        val millis = now.toEpochMilli()
        return EventEntity(
            eventId = "test-${UUID.randomUUID()}",
            type = TYPE,
            merchantId = MERCHANT_ID,
            sourcePackage = "io.github.suriyadi15.qrishook",
            sourceApp = "QRIS Hook",
            title = "Webhook test",
            text = "Test payment Rp1000",
            bigText = "Test payment Rp1000",
            amount = 1_000L,
            currency = "IDR",
            senderName = "QRIS Hook",
            paymentSource = "Test",
            receivedAt = now.toString(),
            status = DeliveryStatus.Pending,
            attempts = 0,
            lastError = "",
            lastResponseCode = null,
            lastResponseMessage = "",
            lastResponseBody = "",
            lastWebhookAttemptAtMillis = null,
            createdAtMillis = millis,
            updatedAtMillis = millis,
        )
    }
}
