package io.github.suriyadi15.qrishook.domain

import java.time.Instant
import java.util.UUID

data class QrisPaymentEvent(
    val eventId: String = UUID.randomUUID().toString(),
    val type: String = TYPE,
    val merchantId: String,
    val notification: QrisPaymentNotification,
    val payment: QrisPaymentInfo,
    val receivedAt: Instant,
) {
    companion object {
        const val TYPE = "qris.payment.success"
    }
}

data class QrisPaymentNotification(
    val sourcePackage: String,
    val sourceApp: String,
    val title: String,
    val text: String,
    val bigText: String,
)

data class QrisPaymentInfo(
    val amount: Long,
    val currency: String = "IDR",
    val senderName: String? = null,
    val paymentSource: String? = null,
)
