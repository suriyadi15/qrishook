package io.github.suriyadi15.qrishook.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.suriyadi15.qrishook.domain.DeliveryStatus
import io.github.suriyadi15.qrishook.domain.QrisPaymentEvent
import io.github.suriyadi15.qrishook.domain.QrisPaymentInfo
import io.github.suriyadi15.qrishook.domain.QrisPaymentNotification
import java.time.Instant

@Entity(tableName = "qris_events")
data class EventEntity(
    @PrimaryKey val eventId: String,
    val type: String,
    val merchantId: String,
    val sourcePackage: String,
    val sourceApp: String,
    val title: String,
    val text: String,
    val bigText: String,
    val amount: Long,
    val currency: String,
    val senderName: String?,
    val paymentSource: String?,
    val receivedAt: String,
    val status: DeliveryStatus,
    val attempts: Int,
    val lastError: String,
    val lastResponseCode: Int?,
    val lastResponseMessage: String,
    val lastResponseBody: String,
    val lastWebhookAttemptAtMillis: Long?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
) {
    companion object {
        fun fromEvent(event: QrisPaymentEvent): EventEntity {
            val now = System.currentTimeMillis()
            return EventEntity(
                eventId = event.eventId,
                type = event.type,
                merchantId = event.merchantId,
                sourcePackage = event.notification.sourcePackage,
                sourceApp = event.notification.sourceApp,
                title = event.notification.title,
                text = event.notification.text,
                bigText = event.notification.bigText,
                amount = event.payment.amount,
                currency = event.payment.currency,
                senderName = event.payment.senderName,
                paymentSource = event.payment.paymentSource,
                receivedAt = event.receivedAt.toString(),
                status = DeliveryStatus.Pending,
                attempts = 0,
                lastError = "",
                lastResponseCode = null,
                lastResponseMessage = "",
                lastResponseBody = "",
                lastWebhookAttemptAtMillis = null,
                createdAtMillis = now,
                updatedAtMillis = now,
            )
        }
    }

    fun toEvent(): QrisPaymentEvent = QrisPaymentEvent(
        eventId = eventId,
        type = type,
        merchantId = merchantId,
        notification = QrisPaymentNotification(
            sourcePackage = sourcePackage,
            sourceApp = sourceApp,
            title = title,
            text = text,
            bigText = bigText,
        ),
        payment = QrisPaymentInfo(
            amount = amount,
            currency = currency,
            senderName = senderName,
            paymentSource = paymentSource,
        ),
        receivedAt = Instant.parse(receivedAt),
    )
}
