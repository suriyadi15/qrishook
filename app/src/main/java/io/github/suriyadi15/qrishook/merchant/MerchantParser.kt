package io.github.suriyadi15.qrishook.merchant

import io.github.suriyadi15.qrishook.domain.ObservedNotification
import io.github.suriyadi15.qrishook.domain.QrisPaymentEvent
import io.github.suriyadi15.qrishook.domain.QrisPaymentInfo
import io.github.suriyadi15.qrishook.domain.QrisPaymentNotification
import java.time.Instant

abstract class MerchantParser {
    abstract val merchantId: String
    abstract val displayName: String
    abstract val merchantPackages: List<String>

    fun parse(notification: ObservedNotification): QrisPaymentEvent? {
        val payment = parsePaymentInfo(notification) ?: return null

        return QrisPaymentEvent(
            merchantId = merchantId,
            notification = QrisPaymentNotification(
                sourcePackage = notification.sourcePackage,
                sourceApp = notification.sourceApp,
                title = notification.title,
                text = notification.text,
                bigText = notification.bigText,
            ),
            payment = payment,
            receivedAt = Instant.ofEpochMilli(notification.postedAtMillis),
        )
    }

    protected abstract fun parsePaymentInfo(notification: ObservedNotification): QrisPaymentInfo?

    open fun canHandle(notification: ObservedNotification): Boolean {
        return merchantPackages.contains(notification.sourcePackage)
    }
}
