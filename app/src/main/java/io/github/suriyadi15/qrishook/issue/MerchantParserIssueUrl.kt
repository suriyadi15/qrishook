package io.github.suriyadi15.qrishook.issue

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class MerchantParserIssueInfo(
    val appVersion: String,
    val androidVersion: String,
    val merchantAppName: String? = null,
    val packageName: String? = null,
    val debugPayload: String? = null,
)

data class MerchantParserIssueLink(
    val url: String,
    val payloadIncluded: Boolean,
)

object MerchantParserIssueUrl {
    const val BASE_URL = "https://github.com/suriyadi15/qrishook/issues/new"
    const val TEMPLATE = "merchant_parser_request.yml"
    const val MAX_URL_LENGTH = 8000

    fun build(info: MerchantParserIssueInfo): MerchantParserIssueLink {
        val params = linkedMapOf(
            "template" to TEMPLATE,
            "title" to info.merchantAppName?.takeIf { it.isNotBlank() }?.let { "[Merchant Parser] $it" },
            "merchant_app_name" to info.merchantAppName,
            "package_name" to info.packageName,
            "qris_hook_version" to info.appVersion,
            "android_version" to info.androidVersion,
        )

        val withoutPayload = buildUrl(params)
        val payload = info.debugPayload?.takeIf { it.isNotBlank() }
            ?: return MerchantParserIssueLink(withoutPayload, payloadIncluded = false)

        val withPayload = buildUrl(params + ("debug_payload" to payload))
        return if (withPayload.length <= MAX_URL_LENGTH) {
            MerchantParserIssueLink(withPayload, payloadIncluded = true)
        } else {
            MerchantParserIssueLink(withoutPayload, payloadIncluded = false)
        }
    }

    private fun buildUrl(params: Map<String, String?>): String {
        return params.entries
            .filter { !it.value.isNullOrBlank() }
            .joinToString(separator = "&", prefix = "$BASE_URL?") { (key, value) ->
                "${encode(key)}=${encode(value.orEmpty())}"
            }
    }

    private fun encode(value: String): String {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.name()).replace("+", "%20")
    }
}
