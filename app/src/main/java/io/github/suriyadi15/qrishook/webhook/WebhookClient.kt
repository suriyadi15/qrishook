package io.github.suriyadi15.qrishook.webhook

import io.github.suriyadi15.qrishook.data.AppSettings
import io.github.suriyadi15.qrishook.data.EventEntity
import io.github.suriyadi15.qrishook.data.WebhookEndpoint
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
        val endpoint = settings.webhookFor(event.merchantId)
            ?: return WebhookResult.Failure(
                reason = "No webhook configured for merchant ${event.merchantId}",
                code = null,
                message = "",
                body = "",
            )

        return post(endpoint, WebhookPayloadBuilder.build(event))
    }

    fun sendTest(endpoint: WebhookEndpoint): WebhookResult {
        return post(endpoint, WebhookPayloadBuilder.build(WebhookTestEvent.create()))
    }

    private fun post(endpoint: WebhookEndpoint, payload: String): WebhookResult {
        val url = endpoint.url
        if (url.isBlank()) {
            return WebhookResult.Failure(
                reason = "Webhook URL is not configured",
                code = null,
                message = "",
                body = "",
                webhookUrl = url,
            )
        }

        val requestBuilder = runCatching {
            Request.Builder()
                .url(url)
                .post(payload.toRequestBody(JSON))
                .header("Content-Type", "application/json")
        }.getOrElse { error ->
            return WebhookResult.Failure(
                reason = error.message ?: error::class.java.simpleName,
                code = null,
                message = error::class.java.simpleName,
                body = "",
                webhookUrl = url,
            )
        }

        if (endpoint.secret.isNotBlank()) {
            requestBuilder.header("X-Webhook-Secret", endpoint.secret)
        }

        return runCatching {
            client.newCall(requestBuilder.build()).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    WebhookResult.Success(
                        code = response.code,
                        message = response.message,
                        body = responseBody,
                        webhookUrl = url,
                    )
                } else {
                    WebhookResult.Failure(
                        reason = "HTTP ${response.code}",
                        code = response.code,
                        message = response.message,
                        body = responseBody,
                        webhookUrl = url,
                    )
                }
            }
        }.getOrElse { error ->
            WebhookResult.Failure(
                reason = error.message ?: error::class.java.simpleName,
                code = null,
                message = error::class.java.simpleName,
                body = "",
                webhookUrl = url,
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
        val webhookUrl: String = "",
    ) : WebhookResult

    data class Failure(
        val reason: String,
        val code: Int?,
        val message: String,
        val body: String,
        val webhookUrl: String = "",
    ) : WebhookResult
}
