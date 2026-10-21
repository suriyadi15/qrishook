package io.github.suriyadi15.qrishook.issue

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

class MerchantParserRequestClientTest {
    @Test
    fun `builds request body from info`() {
        val body = JSONObject(
            MerchantParserRequestClient.buildBody(
                MerchantParserRequestInfo(
                    appVersion = "1.2.3",
                    androidVersion = "Android 14, Google Pixel 7",
                    merchantAppName = "InterActive QRIS",
                    packageName = "com.interactive.qrisid",
                    debugPayload = "{\"a\":1}",
                    expectedDetails = "  Amount: Rp10.000  ",
                    contact = " budi@example.com ",
                ),
            ),
        )

        assertEquals("InterActive QRIS", body.getString("merchantAppName"))
        assertEquals("com.interactive.qrisid", body.getString("packageName"))
        assertEquals("Amount: Rp10.000", body.getString("expectedDetails"))
        assertEquals("budi@example.com", body.getString("contact"))
        assertEquals("{\"a\":1}", body.getString("debugPayload"))
        assertEquals("1.2.3", body.getString("appVersion"))
        assertEquals("Android 14, Google Pixel 7", body.getString("androidVersion"))
    }
}
