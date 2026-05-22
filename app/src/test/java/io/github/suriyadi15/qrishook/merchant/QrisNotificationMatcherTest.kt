package io.github.suriyadi15.qrishook.merchant

import io.github.suriyadi15.qrishook.domain.ObservedNotification
import io.github.suriyadi15.qrishook.domain.QrisPaymentInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

class QrisNotificationMatcherTest {
    private val matcher = QrisNotificationMatcher()
    private val parser = MerchantRegistry.builtInParsers
        .first { it.merchantId == "interactive_qris" }

    @Test
    fun matchesSuccessfulQrisNotification() {
        val events = matcher.match(
            notification = notification(
                title = "Pembayaran QRIS berhasil",
                text = "Pembayaran QRIS sebesar Rp. 10.000 Sample Sender - Sample Source telah diterima",
            ),
            merchants = listOf(parser),
        )

        val event = events.singleOrNull()
        assertNotNull(event)
        assertEquals(10_000L, event?.payment?.amount)
        assertEquals("interactive_qris", event?.merchantId)
    }

    @Test
    fun ignoresRefundOrFailedNotification() {
        val failed = matcher.match(
            notification = notification(
                title = "Pembayaran QRIS gagal",
                text = "Transaksi Rp10.000 dibatalkan",
            ),
            merchants = listOf(parser),
        )

        val refund = matcher.match(
            notification = notification(
                title = "Refund QRIS",
                text = "Refund Rp10.000 berhasil",
            ),
            merchants = listOf(parser),
        )

        assertEquals(emptyList<Any>(), failed)
        assertEquals(emptyList<Any>(), refund)
    }

    @Test
    fun ignoresDifferentPackage() {
        val events = matcher.match(
            notification = notification(
                sourcePackage = "com.other.app",
                title = "Pembayaran QRIS berhasil",
                text = "Pembayaran QRIS sebesar Rp. 10.000 Sample Sender - Sample Source telah diterima",
            ),
            merchants = listOf(parser),
        )

        assertEquals(emptyList<Any>(), events)
    }

    @Test
    fun returnsAllEventsFromValidParsers() {
        val secondParser = object : MerchantParser() {
            override val merchantId = "second_merchant"
            override val displayName = "Second Merchant"
            override val merchantPackages = listOf("com.interactive.qrisid")

            override fun parsePaymentInfo(notification: ObservedNotification): QrisPaymentInfo? {
                val amount = AmountParser.parse(notification.combinedText, AmountParser.DEFAULT_PATTERNS)
                    ?: return null

                return QrisPaymentInfo(amount = amount)
            }
        }

        val events = matcher.match(
            notification = notification(
                title = "Pembayaran QRIS berhasil",
                text = "Pembayaran QRIS sebesar Rp. 10.000 Sample Sender - Sample Source telah diterima",
            ),
            merchants = listOf(parser, secondParser),
        )

        assertEquals(2, events.size)
        assertEquals(listOf("interactive_qris", "second_merchant"), events.map { it.merchantId })
    }

    @Test
    fun skipsParseWhenCanHandleIsFalse() {
        var parsePaymentInfoCalled = false
        val parser = object : MerchantParser() {
            override val merchantId = "test_merchant"
            override val displayName = "Test Merchant"
            override val merchantPackages = listOf("com.expected.app")

            override fun parsePaymentInfo(notification: ObservedNotification): QrisPaymentInfo? {
                parsePaymentInfoCalled = true
                return null
            }
        }

        val events = matcher.match(
            notification = notification(
                sourcePackage = "com.other.app",
                title = "Pembayaran QRIS berhasil",
                text = "Pembayaran QRIS sebesar Rp. 10.000 Sample Sender - Sample Source telah diterima",
            ),
            merchants = listOf(parser),
        )

        assertEquals(emptyList<Any>(), events)
        assertFalse(parsePaymentInfoCalled)
    }

    @Test
    fun emptySelectedParsersReturnsNoEvents() {
        val events = matcher.match(
            notification = notification(
                title = "Pembayaran QRIS berhasil",
                text = "Pembayaran QRIS sebesar Rp. 10.000 Sample Sender - Sample Source telah diterima",
            ),
            merchants = emptyList(),
        )

        assertEquals(emptyList<Any>(), events)
    }

    private fun notification(
        sourcePackage: String = "com.interactive.qrisid",
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
