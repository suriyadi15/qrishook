package io.github.suriyadi15.qrishook.issue

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Test

class PayloadRedactorTest {
    @Test
    fun `masks long numbers and phone numbers in text`() {
        assertEquals(
            "Transaksi ${PayloadRedactor.REDACTED_NUMBER} dari ${PayloadRedactor.REDACTED_NUMBER}",
            PayloadRedactor.redactText("Transaksi 202605210001234 dari 0812-3456-7890"),
        )
        assertEquals(
            "Hubungi ${PayloadRedactor.REDACTED_NUMBER}",
            PayloadRedactor.redactText("Hubungi +6281234567890"),
        )
    }

    @Test
    fun `keeps amounts dates and short numbers`() {
        val text = "2030-01-01 12:00:00 - Pembayaran Rp 10.000 diterima. Call Center BRI 150017"

        assertEquals(text, PayloadRedactor.redactText(text))
    }

    @Test
    fun `masks email addresses`() {
        assertEquals(
            "Dari ${PayloadRedactor.REDACTED_EMAIL}",
            PayloadRedactor.redactText("Dari budi@example.com"),
        )
    }

    @Test
    fun `redacts strings recursively and blanks sensitive extras`() {
        val payload = """
            {
              "source_package": "com.example.merchant",
              "notification": { "post_time": 1779340800000, "id": 7 },
              "text": { "text": "Rp 1 dari BUDI, ref 1234567890", "text_lines": ["0812 3456 7890"] },
              "extras": {
                "android.people.list": ["BUDI"],
                "android.selfDisplayName": "Budi",
                "android.messages": null,
                "android.title": "Pembayaran diterima"
              }
            }
        """.trimIndent()

        val result = JSONObject(PayloadRedactor.redact(payload))

        assertEquals("com.example.merchant", result.getString("source_package"))
        assertEquals(1779340800000L, result.getJSONObject("notification").getLong("post_time"))
        assertEquals(
            "Rp 1 dari BUDI, ref ${PayloadRedactor.REDACTED_NUMBER}",
            result.getJSONObject("text").getString("text"),
        )
        assertEquals(
            PayloadRedactor.REDACTED_NUMBER,
            result.getJSONObject("text").getJSONArray("text_lines").getString(0),
        )
        val extras = result.getJSONObject("extras")
        assertEquals(PayloadRedactor.REDACTED, extras.getString("android.people.list"))
        assertEquals(PayloadRedactor.REDACTED, extras.getString("android.selfDisplayName"))
        assertEquals(JSONObject.NULL, extras.get("android.messages"))
        assertEquals("Pembayaran diterima", extras.getString("android.title"))
    }

    @Test
    fun `falls back to text redaction for invalid json`() {
        assertEquals(
            "not json ${PayloadRedactor.REDACTED_NUMBER}",
            PayloadRedactor.redact("not json 1234567890"),
        )
    }
}
