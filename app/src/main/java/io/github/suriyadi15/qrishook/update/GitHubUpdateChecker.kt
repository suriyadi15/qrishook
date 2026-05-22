package io.github.suriyadi15.qrishook.update

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GitHubUpdateChecker(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build(),
) {
    fun check(currentVersion: String): AppUpdateState {
        val request = Request.Builder()
            .url(LATEST_RELEASE_URL)
            .header("Accept", "application/vnd.github+json")
            .build()

        return runCatching {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    return AppUpdateState.Error("Failed to check update: HTTP ${response.code}")
                }

                val release = parseRelease(body)
                    ?: return AppUpdateState.Error("Failed to read latest release.")

                if (AppVersion.compare(release.version, currentVersion) > 0) {
                    AppUpdateState.UpdateAvailable(
                        latestVersion = release.version,
                        downloadUrl = release.apkDownloadUrl ?: release.htmlUrl,
                    )
                } else {
                    AppUpdateState.UpToDate(latestVersion = release.version)
                }
            }
        }.getOrElse { error ->
            AppUpdateState.Error(error.message ?: error::class.java.simpleName)
        }
    }

    private fun parseRelease(body: String): GitHubRelease? {
        val json = JSONObject(body)
        val tagName = json.optString("tag_name").ifBlank { return null }
        val htmlUrl = json.optString("html_url").ifBlank { return null }
        val assets = json.optJSONArray("assets")
        var apkDownloadUrl: String? = null

        if (assets != null) {
            for (index in 0 until assets.length()) {
                val asset = assets.optJSONObject(index) ?: continue
                val name = asset.optString("name")
                val state = asset.optString("state")
                val downloadUrl = asset.optString("browser_download_url")
                if (name.endsWith(".apk", ignoreCase = true) &&
                    state.equals("uploaded", ignoreCase = true) &&
                    downloadUrl.isNotBlank()
                ) {
                    apkDownloadUrl = downloadUrl
                    break
                }
            }
        }

        return GitHubRelease(
            version = tagName,
            htmlUrl = htmlUrl,
            apkDownloadUrl = apkDownloadUrl,
        )
    }

    private data class GitHubRelease(
        val version: String,
        val htmlUrl: String,
        val apkDownloadUrl: String?,
    )

    private companion object {
        const val LATEST_RELEASE_URL =
            "https://api.github.com/repos/suriyadi15/qrishook/releases/latest"
    }
}
