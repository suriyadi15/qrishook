package io.github.suriyadi15.qrishook.webhook

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

sealed interface WebhookUrlValidation {
    data object Valid : WebhookUrlValidation
    data object Insecure : WebhookUrlValidation
    data object Empty : WebhookUrlValidation
    data object Invalid : WebhookUrlValidation
}

object WebhookUrlValidator {
    fun validate(url: String): WebhookUrlValidation {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return WebhookUrlValidation.Empty

        val parsed = trimmed.toHttpUrlOrNull() ?: return WebhookUrlValidation.Invalid
        if (parsed.host.isBlank()) return WebhookUrlValidation.Invalid
        return if (parsed.isHttps) WebhookUrlValidation.Valid else WebhookUrlValidation.Insecure
    }

    /** Host plus path, used as a compact label and as the fallback webhook name. */
    fun displayHost(url: String): String {
        return url.trim().toHttpUrlOrNull()?.host ?: url.trim()
    }
}
