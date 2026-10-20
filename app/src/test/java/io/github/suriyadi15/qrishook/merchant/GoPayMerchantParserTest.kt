package io.github.suriyadi15.qrishook.merchant

import io.github.suriyadi15.qrishook.domain.ObservedNotification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GoPayMerchantParserTest {
    @Test
    fun parserIsRegisteredAndParsesCapturedSample() {
        assertTrue(MerchantRegistry.builtInParsers.any { it.merchantId == "gopay_merchant" })
        assertTrue(GoPayMerchantParser.merchantPackages.contains("com.gojek.gopaymerchant"))

        val event = GoPayMerchantParser.parse(notification(text = sampleText(amount = "1")))

        assertNotNull(event)
        assertEquals("gopay_merchant", event?.merchantId)
        assertEquals(1L, event?.payment?.amount)
        assertEquals("IDR", event?.payment?.currency)
        assertNull(event?.payment?.senderName)
        assertNull(event?.payment?.paymentSource)
    }

    @Test
    fun parsesThousandsSeparatedAmount() {
        val event = GoPayMerchantParser.parse(notification(text = sampleText(amount = "25.000")))

        assertEquals(25_000L, event?.payment?.amount)
    }

    @Test
    fun canHandleRejectsDifferentPackage() {
        assertFalse(
            GoPayMerchantParser.canHandle(
                notification(sourcePackage = "com.other.app", text = sampleText(amount = "1")),
            ),
        )
    }

    @Test
    fun rejectsNonPaymentNotification() {
        val event = GoPayMerchantParser.parse(
            notification(
                title = "Promo spesial buat kamu",
                text = "Rp 10.000 di SAMPLE MERCHANT jadi lebih hemat.",
            ),
        )

        assertNull(event)
    }

    @Test
    fun rejectsNotificationWithoutAmount() {
        val event = GoPayMerchantParser.parse(notification(text = "Pembayaran di SAMPLE MERCHANT."))

        assertNull(event)
    }

    private fun sampleText(amount: String) = "Rp $amount di SAMPLE MERCHANT."

    private fun notification(
        text: String,
        title: String = "Pembayaran QRIS statis diterima",
        sourcePackage: String = "com.gojek.gopaymerchant",
    ) = ObservedNotification(
        sourcePackage = sourcePackage,
        sourceApp = "GoPay Merchant",
        title = title,
        text = text,
        bigText = text,
        postedAtMillis = 1_700_000_000_000,
    )
}
