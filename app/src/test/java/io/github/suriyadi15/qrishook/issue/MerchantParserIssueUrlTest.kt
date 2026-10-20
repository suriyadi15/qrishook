package io.github.suriyadi15.qrishook.issue

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MerchantParserIssueUrlTest {
    @Test
    fun `builds url with app and device info only`() {
        val link = MerchantParserIssueUrl.build(
            MerchantParserIssueInfo(
                appVersion = "1.2.3",
                androidVersion = "Android 14, Google Pixel 7",
            ),
        )

        assertEquals(
            "https://github.com/suriyadi15/qrishook/issues/new" +
                "?template=merchant_parser_request.yml" +
                "&qris_hook_version=1.2.3" +
                "&android_version=Android%2014%2C%20Google%20Pixel%207",
            link.url,
        )
        assertFalse(link.payloadIncluded)
    }

    @Test
    fun `prefills merchant fields and encodes payload from debug log`() {
        val link = MerchantParserIssueUrl.build(
            MerchantParserIssueInfo(
                appVersion = "1.2.3",
                androidVersion = "Android 14, Pixel 7",
                merchantAppName = "BRI Merchant",
                packageName = "id.co.bri.merchant",
                debugPayload = "{\n  \"a\": \"b c\"\n}",
            ),
        )

        assertTrue(link.payloadIncluded)
        assertTrue(link.url.contains("&title=%5BMerchant%20Parser%5D%20BRI%20Merchant"))
        assertTrue(link.url.contains("&merchant_app_name=BRI%20Merchant"))
        assertTrue(link.url.contains("&package_name=id.co.bri.merchant"))
        assertTrue(link.url.contains("&debug_payload=%7B%0A%20%20%22a%22%3A%20%22b%20c%22%0A%7D"))
    }

    @Test
    fun `drops payload when url would be too long`() {
        val link = MerchantParserIssueUrl.build(
            MerchantParserIssueInfo(
                appVersion = "1.2.3",
                androidVersion = "Android 14",
                merchantAppName = "BRI Merchant",
                packageName = "id.co.bri.merchant",
                debugPayload = "x".repeat(20_000),
            ),
        )

        assertFalse(link.payloadIncluded)
        assertFalse(link.url.contains("debug_payload"))
        assertTrue(link.url.contains("&package_name=id.co.bri.merchant"))
        assertTrue(link.url.length <= MerchantParserIssueUrl.MAX_URL_LENGTH)
    }
}
