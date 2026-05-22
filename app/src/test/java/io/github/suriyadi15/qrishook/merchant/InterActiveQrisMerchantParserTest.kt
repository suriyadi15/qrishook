package io.github.suriyadi15.qrishook.merchant

import io.github.suriyadi15.qrishook.domain.ObservedNotification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InterActiveQrisMerchantParserTest {
    @Test
    fun parserIsRegisteredAndParsesCapturedSample() {
        assertTrue(MerchantRegistry.builtInParsers.any { it.merchantId == "interactive_qris" })
        assertTrue(InterActiveQrisMerchantParser.merchantPackages.contains("com.interactive.qrisid"))

        val event = InterActiveQrisMerchantParser.parse(
            notification(
                sourcePackage = "com.interactive.qrisid",
                title = "Transaksi InterActive QRIS",
                text = "Pembayaran QRIS sebesar Rp 1 Sample Sender - Sample Source telah diterima",
            ),
        )

        assertNotNull(event)
        assertEquals("interactive_qris", event?.merchantId)
        assertEquals(1L, event?.payment?.amount)
        assertEquals("IDR", event?.payment?.currency)
        assertEquals("Sample Sender", event?.payment?.senderName)
        assertEquals("Sample Source", event?.payment?.paymentSource)
    }

    @Test
    fun canHandleRejectsDifferentPackage() {
        assertFalse(
            InterActiveQrisMerchantParser.canHandle(
                notification(
                    sourcePackage = "com.other.app",
                    title = "Transaksi InterActive QRIS",
                    text = "Pembayaran QRIS sebesar Rp 1 Sample Sender - Sample Source telah diterima",
                ),
            ),
        )
    }

    @Test
    fun rejectsNotificationWithoutAmount() {
        val event = InterActiveQrisMerchantParser.parse(
            notification(
                sourcePackage = "com.interactive.qrisid",
                title = "Transaksi InterActive QRIS",
                text = "Pembayaran QRIS Sample Sender - Sample Source telah diterima",
            ),
        )

        assertNull(event)
    }

    @Test
    fun rejectsGenericSuccessText() {
        val event = InterActiveQrisMerchantParser.parse(
            notification(
                sourcePackage = "com.interactive.qrisid",
                title = "Pembayaran QRIS berhasil",
                text = "QRIS dibayar Rp10.000",
            ),
        )

        assertNull(event)
    }

    private fun notification(
        sourcePackage: String,
        title: String,
        text: String,
    ) = ObservedNotification(
        sourcePackage = sourcePackage,
        sourceApp = "Merchant",
        title = title,
        text = text,
        bigText = "",
        postedAtMillis = 1_700_000_000_000,
    )
}
