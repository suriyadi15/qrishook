package io.github.suriyadi15.qrishook.merchant

import io.github.suriyadi15.qrishook.domain.ObservedNotification
import io.github.suriyadi15.qrishook.domain.QrisPaymentInfo

object BriMerchantParser : MerchantParser() {
    override val merchantId = "bri_merchant"
    override val displayName = "BRI Merchant"
    override val merchantPackages = listOf("id.co.bri.merchant")

    private val paymentPattern = Regex(
        """(?i)\bJenis\s+Transaksi\s+PURCHASE\s+sebesar\s+Rp\.?\s*([0-9][0-9.,]*)\s+SUKSES\b""",
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
