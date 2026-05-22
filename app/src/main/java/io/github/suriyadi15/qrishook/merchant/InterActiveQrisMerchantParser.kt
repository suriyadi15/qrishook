package io.github.suriyadi15.qrishook.merchant

import io.github.suriyadi15.qrishook.domain.ObservedNotification
import io.github.suriyadi15.qrishook.domain.QrisPaymentInfo

object InterActiveQrisMerchantParser : MerchantParser() {
    override val merchantId = "interactive_qris"
    override val displayName = "InterActive QRIS"
    override val merchantPackages = listOf("com.interactive.qrisid")

    private val paymentPattern = Regex(
        """(?i)\bPembayaran\s+QRIS\s+sebesar\s+Rp\.?\s*([0-9][0-9.,]*)\s+(.+?)\s+-\s+(.+?)\s+telah\s+diterima\b""",
    )

    override fun parsePaymentInfo(notification: ObservedNotification): QrisPaymentInfo? {
        val match = paymentPattern.find(notification.combinedText) ?: return null
        val amount = parseAmount(match.groupValues[1])
            ?: return null
        val senderName = match.groupValues[2].trim().ifBlank { null }
        val paymentSource = match.groupValues[3].trim().ifBlank { null }

        return QrisPaymentInfo(
            amount = amount,
            senderName = senderName,
            paymentSource = paymentSource,
        )
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
