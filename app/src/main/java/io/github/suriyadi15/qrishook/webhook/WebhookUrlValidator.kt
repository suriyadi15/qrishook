package io.github.suriyadi15.qrishook.webhook

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

sealed interface WebhookUrlValidation {
    data object Valid : WebhookUrlValidation
    data object Insecure : WebhookUrlValidation

    /** http:// URL in a build that blocks cleartext traffic (release builds). */
    data object CleartextBlocked : WebhookUrlValidation
    data object Empty : WebhookUrlValidation
    data object Invalid : WebhookUrlValidation
}

object WebhookUrlValidator {
    fun validate(url: String, allowCleartext: Boolean = true): WebhookUrlValidation {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return WebhookUrlValidation.Empty

        val parsed = trimmed.toHttpUrlOrNull() ?: return WebhookUrlValidation.Invalid
        if (parsed.host.isBlank()) return WebhookUrlValidation.Invalid
        return when {
            parsed.isHttps -> WebhookUrlValidation.Valid
            allowCleartext -> WebhookUrlValidation.Insecure
            else -> WebhookUrlValidation.CleartextBlocked
        }
    }

    /** Host plus path, used as a compact label and as the fallback webhook name. */
    fun displayHost(url: String): String {
        return url.trim().toHttpUrlOrNull()?.host ?: url.trim()
    }
}
