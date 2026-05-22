package io.github.suriyadi15.qrishook.data

import io.github.suriyadi15.qrishook.domain.DeliveryStatus
import io.github.suriyadi15.qrishook.domain.QrisPaymentEvent
import io.github.suriyadi15.qrishook.domain.QrisPaymentInfo
import io.github.suriyadi15.qrishook.domain.QrisPaymentNotification
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class EventEntityTest {
    @Test
    fun mapsNestedPaymentEventToFlatEntityAndBack() {
        val event = QrisPaymentEvent(
            eventId = "event-1",
            merchantId = "interactive_qris",
            notification = QrisPaymentNotification(
                sourcePackage = "com.interactive.qrisid",
                sourceApp = "InterActive QRIS",
                title = "Transaksi InterActive QRIS",
                text = "Pembayaran QRIS sebesar Rp 1 Sample Sender - Sample Source telah diterima",
                bigText = "Pembayaran QRIS sebesar Rp 1 Sample Sender - Sample Source telah diterima",
            ),
            payment = QrisPaymentInfo(
                amount = 1L,
                senderName = "Sample Sender",
                paymentSource = "Sample Source",
            ),
            receivedAt = Instant.parse("2026-05-21T12:00:00Z"),
        )

        val entity = EventEntity.fromEvent(event)

        assertEquals("qris.payment.success", entity.type)
        assertEquals("com.interactive.qrisid", entity.sourcePackage)
        assertEquals(1L, entity.amount)
        assertEquals("IDR", entity.currency)
        assertEquals("Sample Sender", entity.senderName)
        assertEquals("Sample Source", entity.paymentSource)
        assertEquals(DeliveryStatus.Pending, entity.status)
        assertEquals(null, entity.lastResponseCode)
        assertEquals("", entity.lastResponseMessage)
        assertEquals("", entity.lastResponseBody)
        assertEquals(null, entity.lastWebhookAttemptAtMillis)

        val restored = entity.toEvent()
        assertEquals(event.eventId, restored.eventId)
        assertEquals(event.type, restored.type)
        assertEquals(event.merchantId, restored.merchantId)
        assertEquals(event.notification, restored.notification)
        assertEquals(event.payment, restored.payment)
        assertEquals(event.receivedAt, restored.receivedAt)
    }
}
