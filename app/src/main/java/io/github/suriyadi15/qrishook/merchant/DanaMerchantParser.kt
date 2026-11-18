package io.github.suriyadi15.qrishook.merchant

import io.github.suriyadi15.qrishook.domain.ObservedNotification
import io.github.suriyadi15.qrishook.domain.QrisPaymentInfo

object DanaMerchantParser : MerchantParser() {
    override val merchantId = "dana_bisnis"
    override val displayName = "DANA Bisnis"
    override val merchantPackages = listOf("id.dana")

    private val paymentPattern = Regex(
        """(?is)\bPembayaran\s+Masuk\b.*?\bRp\.?\s*([0-9][0-9.,]*)\s+diterima\s+DANA\s+Bisnis\b""",
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
