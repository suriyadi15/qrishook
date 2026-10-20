package io.github.suriyadi15.qrishook.merchant

import io.github.suriyadi15.qrishook.domain.ObservedNotification
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BriMerchantParserTest {
    @Test
    fun parserIsRegisteredAndParsesCapturedSample() {
        assertTrue(MerchantRegistry.builtInParsers.any { it.merchantId == "bri_merchant" })
        assertTrue(BriMerchantParser.merchantPackages.contains("id.co.bri.merchant"))

        val event = BriMerchantParser.parse(
            notification(text = sampleText(type = "PURCHASE", amount = "40000", status = "SUKSES")),
        )

        assertNotNull(event)
        assertEquals("bri_merchant", event?.merchantId)
        assertEquals(40_000L, event?.payment?.amount)
        assertEquals("IDR", event?.payment?.currency)
        assertNull(event?.payment?.senderName)
        assertNull(event?.payment?.paymentSource)
    }

    @Test
    fun parsesThousandsSeparatedAmount() {
        val event = BriMerchantParser.parse(
            notification(text = sampleText(type = "PURCHASE", amount = "1.217", status = "SUKSES")),
        )

        assertEquals(1_217L, event?.payment?.amount)
    }

    @Test
    fun canHandleRejectsDifferentPackage() {
        assertFalse(
            BriMerchantParser.canHandle(
                notification(
                    sourcePackage = "com.other.app",
                    text = sampleText(type = "PURCHASE", amount = "40000", status = "SUKSES"),
                ),
            ),
        )
    }

    @Test
    fun rejectsNonPurchaseTransaction() {
        val event = BriMerchantParser.parse(
            notification(text = sampleText(type = "REFUND", amount = "40000", status = "SUKSES")),
        )

        assertNull(event)
    }

    @Test
    fun rejectsUnsuccessfulTransaction() {
        val event = BriMerchantParser.parse(
            notification(text = sampleText(type = "PURCHASE", amount = "40000", status = "GAGAL")),
        )

        assertNull(event)
    }

    @Test
    fun rejectsNotificationWithoutAmount() {
        val event = BriMerchantParser.parse(
            notification(text = "Halo Sobat BRI Merchant SAMPLE MERCHANT. Jenis Transaksi PURCHASE SUKSES."),
        )

        assertNull(event)
    }

    private fun sampleText(type: String, amount: String, status: String) =
        "2030-01-01 12:00:00 - Halo Sobat BRI Merchant SAMPLE MERCHANT. " +
            "Jenis Transaksi $type sebesar Rp $amount $status. " +
            "Info lebih lanjut hubungi Call Center BRI 150017"

    private fun notification(
        text: String,
        sourcePackage: String = "id.co.bri.merchant",
    ) = ObservedNotification(
        sourcePackage = sourcePackage,
        sourceApp = "BRI Merchant",
        title = "BRI Merchant",
        text = text,
        bigText = text,
        postedAtMillis = 1_700_000_000_000,
    )
}
