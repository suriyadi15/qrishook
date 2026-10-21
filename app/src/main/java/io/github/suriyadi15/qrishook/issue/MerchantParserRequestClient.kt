package io.github.suriyadi15.qrishook.issue

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

data class MerchantParserRequestInfo(
    val appVersion: String,
    val androidVersion: String,
    val merchantAppName: String,
    val packageName: String,
    val debugPayload: String,
    val expectedDetails: String = "",
    val contact: String = "",
)

sealed interface MerchantParserRequestResult {
    data class Success(val id: Int) : MerchantParserRequestResult
    data class Failure(val message: String) : MerchantParserRequestResult
}

class MerchantParserRequestClient(
    private val endpoint: String = ENDPOINT,
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()

    fun submit(info: MerchantParserRequestInfo): MerchantParserRequestResult {
        val request = Request.Builder()
            .url(endpoint)
            .post(buildBody(info).toRequestBody(JSON))
            .header("X-QrisHook-Client", "android/${info.appVersion}")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (response.isSuccessful) {
                    val id = runCatching { JSONObject(body).getInt("id") }.getOrNull()
                    if (id != null) {
                        MerchantParserRequestResult.Success(id)
                    } else {
                        MerchantParserRequestResult.Failure("Unexpected server response")
                    }
                } else {
                    val reason = runCatching { JSONObject(body).getString("error") }.getOrNull()
                    MerchantParserRequestResult.Failure(reason ?: "Server error (HTTP ${response.code})")
                }
            }
        } catch (error: IOException) {
            MerchantParserRequestResult.Failure("Could not reach server. Check your connection and try again.")
        }
    }

    companion object {
        const val ENDPOINT = "https://qrishook.pages.dev/api/merchant-request"
        private val JSON = "application/json; charset=utf-8".toMediaType()

        fun buildBody(info: MerchantParserRequestInfo): String {
            return JSONObject()
                .put("merchantAppName", info.merchantAppName)
                .put("packageName", info.packageName)
                .put("expectedDetails", info.expectedDetails.trim())
                .put("contact", info.contact.trim())
                .put("debugPayload", info.debugPayload)
                .put("appVersion", info.appVersion)
                .put("androidVersion", info.androidVersion)
                .toString()
        }
    }
}
