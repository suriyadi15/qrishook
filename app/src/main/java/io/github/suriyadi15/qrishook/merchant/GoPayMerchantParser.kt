package io.github.suriyadi15.qrishook.merchant

import io.github.suriyadi15.qrishook.domain.ObservedNotification
import io.github.suriyadi15.qrishook.domain.QrisPaymentInfo

object GoPayMerchantParser : MerchantParser() {
    override val merchantId = "gopay_merchant"
    override val displayName = "GoPay Merchant"
    override val merchantPackages = listOf("com.gojek.gopaymerchant")

    private val paymentPattern = Regex(
        """(?is)\bPembayaran\s+QRIS\b[^\n]*\bditerima\b.*?\bRp\.?\s*([0-9][0-9.,]*)\s+di\b""",
    )

    override fun parsePaymentInfo(notification: ObservedNotification): QrisPaymentInfo? {
        val match = paymentPattern.find(notification.combinedText) ?: return null
        val amount = parseAmount(match.groupValues[1])
            ?: return null

        return QrisPaymentInfo(amount = amount)
    }

    private fun parseAmount(raw: String): Long? {
        val amount = raw
            .replace(".", "")
            .replace(",", "")
            .filter { it.isDigit() }
            .toLongOrNull()
        return amount?.takeIf { it > 0 }
    }
}
