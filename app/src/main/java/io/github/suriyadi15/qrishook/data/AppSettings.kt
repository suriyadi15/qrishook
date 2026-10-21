package io.github.suriyadi15.qrishook.data

data class WebhookEndpoint(
    val id: String,
    val name: String,
    val url: String,
    val secret: String = "",
)

data class AppSettings(
    val webhooks: List<WebhookEndpoint> = emptyList(),
    val defaultWebhookId: String? = null,
    val merchantWebhookIds: Map<String, String> = emptyMap(),
    val selectedMerchantIds: Set<String> = emptySet(),
    val qrisHookActive: Boolean = true,
    val debugModeEnabled: Boolean = false,
    val debugWatchedPackages: Set<String> = emptySet(),
) {
    val defaultWebhook: WebhookEndpoint?
        get() = webhooks.firstOrNull { it.id == defaultWebhookId }

    /** Mapping merchant first, then the default webhook; null when neither resolves. */
    fun webhookFor(merchantId: String): WebhookEndpoint? {
        val mapped = merchantWebhookIds[merchantId]?.let { id -> webhooks.firstOrNull { it.id == id } }
        return mapped ?: defaultWebhook
    }

    /** Selected merchants that explicitly route to [webhookId]. */
    fun merchantsMappedTo(webhookId: String): Set<String> {
        return merchantWebhookIds.filterValues { it == webhookId }.keys
    }

    /** Selected merchants that have neither a mapping nor a default webhook to fall back on. */
    fun unroutedMerchantIds(): Set<String> {
        return selectedMerchantIds.filterTo(linkedSetOf()) { webhookFor(it) == null }
    }

    /**
     * Adds or replaces [endpoint], routes exactly [merchantIds] to it (moving them off any other
     * webhook), and enables those merchants. A mapping that points at [endpoint] but is not in
     * [merchantIds] is dropped, so those merchants fall back to the default webhook.
     */
    fun withWebhookUpserted(
        endpoint: WebhookEndpoint,
        merchantIds: Set<String>,
        makeDefault: Boolean,
    ): AppSettings {
        val nextWebhooks = if (webhooks.any { it.id == endpoint.id }) {
            webhooks.map { if (it.id == endpoint.id) endpoint else it }
        } else {
            webhooks + endpoint
        }
        val nextMapping = merchantWebhookIds
            .filterValues { it != endpoint.id }
            .toMutableMap()
            .apply { merchantIds.forEach { put(it, endpoint.id) } }
        val nextDefault = when {
            makeDefault -> endpoint.id
            defaultWebhookId == endpoint.id -> null
            defaultWebhookId == null && webhooks.isEmpty() -> endpoint.id
            else -> defaultWebhookId
        }
        return copy(
            webhooks = nextWebhooks,
            defaultWebhookId = nextDefault,
            merchantWebhookIds = nextMapping,
            selectedMerchantIds = selectedMerchantIds + merchantIds,
        )
    }

    /** Removes the webhook and every mapping to it; the default moves to the first remaining one. */
    fun withWebhookRemoved(webhookId: String): AppSettings {
        val remaining = webhooks.filterNot { it.id == webhookId }
        val nextDefault = when {
            defaultWebhookId != webhookId -> defaultWebhookId
            else -> remaining.firstOrNull()?.id
        }
        return copy(
            webhooks = remaining,
            defaultWebhookId = nextDefault,
            merchantWebhookIds = merchantWebhookIds.filterValues { it != webhookId },
        )
    }
}
