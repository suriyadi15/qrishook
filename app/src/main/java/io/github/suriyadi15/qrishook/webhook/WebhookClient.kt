package io.github.suriyadi15.qrishook.webhook

import io.github.suriyadi15.qrishook.data.AppSettings
import io.github.suriyadi15.qrishook.data.EventEntity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class WebhookClient : WebhookSender {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun send(event: EventEntity, settings: AppSettings): WebhookResult {
        if (settings.webhookUrl.isBlank()) {
            return WebhookResult.Failure(
                reason = "Webhook URL is not configured",
                code = null,
                message = "",
                body = "",
            )
        }

        val body = WebhookPayloadBuilder
            .build(event)
            .toRequestBody(JSON)

        val requestBuilder = Request.Builder()
            .url(settings.webhookUrl)
            .post(body)
            .header("Content-Type", "application/json")

        if (settings.secret.isNotBlank()) {
            requestBuilder.header("X-Webhook-Secret", settings.secret)
        }

        return runCatching {
            client.newCall(requestBuilder.build()).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    WebhookResult.Success(
                        code = response.code,
                        message = response.message,
                        body = responseBody,
                    )
                } else {
                    WebhookResult.Failure(
                        reason = "HTTP ${response.code}",
                        code = response.code,
                        message = response.message,
                        body = responseBody,
                    )
                }
            }
        }.getOrElse { error ->
            WebhookResult.Failure(
                reason = error.message ?: error::class.java.simpleName,
                code = null,
                message = error::class.java.simpleName,
                body = "",
            )
        }
    }

    private companion object {
        val JSON = "application/json; charset=utf-8".toMediaType()
    }
}

interface WebhookSender {
    fun send(event: EventEntity, settings: AppSettings): WebhookResult
}

sealed interface WebhookResult {
    data class Success(
        val code: Int,
        val message: String,
        val body: String,
    ) : WebhookResult

    data class Failure(
        val reason: String,
        val code: Int?,
        val message: String,
        val body: String,
    ) : WebhookResult
}
