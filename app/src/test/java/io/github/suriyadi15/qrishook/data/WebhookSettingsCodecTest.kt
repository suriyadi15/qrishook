package io.github.suriyadi15.qrishook.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WebhookSettingsCodecTest {
    @Test
    fun webhooksRoundTrip() {
        val webhooks = listOf(
            WebhookEndpoint("a", "Server \"A\"", "https://a.test/hook?x=1", "s3cret"),
            WebhookEndpoint("b", "B", "https://b.test"),
        )

        val decoded = WebhookSettingsCodec.decodeWebhooks(WebhookSettingsCodec.encodeWebhooks(webhooks))

        assertEquals(webhooks, decoded)
    }

    @Test
    fun merchantMappingRoundTrip() {
        val mapping = mapOf("bri" to "a", "gopay" to "b")

        val decoded = WebhookSettingsCodec.decodeMerchantWebhookIds(
            WebhookSettingsCodec.encodeMerchantWebhookIds(mapping),
        )

        assertEquals(mapping, decoded)
    }

    @Test
    fun corruptJsonDecodesToEmpty() {
        assertEquals(emptyList<WebhookEndpoint>(), WebhookSettingsCodec.decodeWebhooks("{not json"))
        assertEquals(emptyList<WebhookEndpoint>(), WebhookSettingsCodec.decodeWebhooks(null))
        assertEquals(emptyMap<String, String>(), WebhookSettingsCodec.decodeMerchantWebhookIds("[oops"))
        assertEquals(emptyMap<String, String>(), WebhookSettingsCodec.decodeMerchantWebhookIds(""))
    }

    @Test
    fun entriesWithoutIdAreSkipped() {
        val decoded = WebhookSettingsCodec.decodeWebhooks(
            """[{"name":"x","url":"https://x.test"},{"id":"ok","name":"y","url":"https://y.test"}]""",
        )

        assertEquals(listOf("ok"), decoded.map { it.id })
    }

    @Test
    fun legacyWebhookKeepsUrlAndSecret() {
        val legacy = WebhookSettingsCodec.legacyWebhook(" https://old.test/hook ", " s ", "Default")

        assertEquals(WebhookEndpoint("default", "Default", "https://old.test/hook", "s"), legacy)
    }

    @Test
    fun blankLegacyUrlProducesNoWebhook() {
        assertNull(WebhookSettingsCodec.legacyWebhook("  ", "secret", "Default"))
        assertNull(WebhookSettingsCodec.legacyWebhook(null, null, "Default"))
    }
}
