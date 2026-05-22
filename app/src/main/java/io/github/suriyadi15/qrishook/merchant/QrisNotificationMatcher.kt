package io.github.suriyadi15.qrishook.merchant

import io.github.suriyadi15.qrishook.domain.ObservedNotification
import io.github.suriyadi15.qrishook.domain.QrisPaymentEvent

class QrisNotificationMatcher {
    fun match(
        notification: ObservedNotification,
        merchants: List<MerchantParser>,
    ): List<QrisPaymentEvent> {
        return merchants
            .filter { merchant -> merchant.canHandle(notification) }
            .mapNotNull { merchant -> merchant.parse(notification) }
    }
}
