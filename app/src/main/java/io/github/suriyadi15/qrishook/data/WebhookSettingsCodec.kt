package io.github.suriyadi15.qrishook.data

import org.json.JSONArray
import org.json.JSONObject

object WebhookSettingsCodec {
    const val LEGACY_WEBHOOK_ID = "default"

    fun encodeWebhooks(webhooks: List<WebhookEndpoint>): String {
        val array = JSONArray()
        webhooks.forEach { endpoint ->
            array.put(
                JSONObject()
                    .put("id", endpoint.id)
                    .put("name", endpoint.name)
                    .put("url", endpoint.url)
                    .put("secret", endpoint.secret),
            )
        }
        return array.toString()
    }

    fun decodeWebhooks(json: String?): List<WebhookEndpoint> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(json)
            (0 until array.length()).mapNotNull { index ->
                val item = array.optJSONObject(index) ?: return@mapNotNull null
                val id = item.optString("id").ifBlank { return@mapNotNull null }
                WebhookEndpoint(
                    id = id,
                    name = item.optString("name"),
                    url = item.optString("url"),
                    secret = item.optString("secret"),
                )
            }
        }.getOrDefault(emptyList())
    }

    fun encodeMerchantWebhookIds(mapping: Map<String, String>): String {
        val obj = JSONObject()
        mapping.forEach { (merchantId, webhookId) -> obj.put(merchantId, webhookId) }
        return obj.toString()
    }

    fun decodeMerchantWebhookIds(json: String?): Map<String, String> {
        if (json.isNullOrBlank()) return emptyMap()
        return runCatching {
            val obj = JSONObject(json)
            obj.keys().asSequence()
                .mapNotNull { key ->
                    val value = obj.optString(key)
                    if (value.isBlank()) null else key to value
                }
                .toMap()
        }.getOrDefault(emptyMap())
    }

    /** Builds the single webhook that pre-multi-webhook installs stored as plain url + secret. */
    fun legacyWebhook(url: String?, secret: String?, name: String): WebhookEndpoint? {
        val trimmedUrl = url.orEmpty().trim()
        if (trimmedUrl.isEmpty()) return null
        return WebhookEndpoint(
            id = LEGACY_WEBHOOK_ID,
            name = name,
            url = trimmedUrl,
            secret = secret.orEmpty().trim(),
        )
    }
}
