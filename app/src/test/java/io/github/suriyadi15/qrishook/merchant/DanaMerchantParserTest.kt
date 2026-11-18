package io.github.suriyadi15.qrishook.merchant

import io.github.suriyadi15.qrishook.domain.ObservedNotification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DanaMerchantParserTest {
    @Test
    fun parserIsRegisteredAndParsesCapturedSample() {
        assertTrue(MerchantRegistry.builtInParsers.any { it.merchantId == "dana_bisnis" })
        assertTrue(DanaMerchantParser.merchantPackages.contains("id.dana"))

        val event = DanaMerchantParser.parse(notification(text = "Rp5.994 diterima DANA Bisnis."))

        assertNotNull(event)
        assertEquals("dana_bisnis", event?.merchantId)
        assertEquals(5_994L, event?.payment?.amount)
        assertEquals("IDR", event?.payment?.currency)
        assertNull(event?.payment?.senderName)
        assertNull(event?.payment?.paymentSource)
    }

    @Test
    fun parsesLargerThousandsSeparatedAmount() {
        val event = DanaMerchantParser.parse(notification(text = "Rp1.250.000 diterima DANA Bisnis."))

        assertEquals(1_250_000L, event?.payment?.amount)
    }

    @Test
    fun canHandleRejectsDifferentPackage() {
        assertFalse(
            DanaMerchantParser.canHandle(
                notification(sourcePackage = "com.other.app", text = "Rp5.994 diterima DANA Bisnis."),
            ),
        )
    }

    @Test
    fun rejectsNonPaymentNotification() {
        val event = DanaMerchantParser.parse(
            notification(title = "Promo DANA", text = "Dapat cashback Rp10.000 untuk kamu."),
        )

        assertNull(event)
    }

    @Test
    fun rejectsPersonalTransferWithoutDanaBisnis() {
        val event = DanaMerchantParser.parse(notification(text = "Rp5.994 diterima dari teman."))

        assertNull(event)
    }

    @Test
    fun rejectsNotificationWithoutAmount() {
        val event = DanaMerchantParser.parse(notification(text = "Pembayaran diterima DANA Bisnis."))

        assertNull(event)
    }

    private fun notification(
        text: String,
        title: String = "Pembayaran Masuk",
        sourcePackage: String = "id.dana",
    ) = ObservedNotification(
        sourcePackage = sourcePackage,
        sourceApp = "DANA",
        title = title,
        text = text,
        bigText = text,
        postedAtMillis = 1_700_000_000_000,
    )
}
