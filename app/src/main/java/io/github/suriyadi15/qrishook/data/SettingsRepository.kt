package io.github.suriyadi15.qrishook.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "qris_hook_settings",
)

class SettingsRepository(context: Context) {
    private val dataStore = context.applicationContext.settingsDataStore

    val settings: Flow<AppSettings> = dataStore.data.map { preferences ->
        val storedWebhooks = preferences[Keys.WebhooksJson]
        val webhooks = if (storedWebhooks != null) {
            WebhookSettingsCodec.decodeWebhooks(storedWebhooks)
        } else {
            // Installs from before multi-webhook support only have a single url + secret.
            listOfNotNull(
                WebhookSettingsCodec.legacyWebhook(
                    url = preferences[Keys.LegacyWebhookUrl],
                    secret = preferences[Keys.LegacySecret],
                    name = LEGACY_WEBHOOK_NAME,
                ),
            )
        }
        val defaultWebhookId = preferences[Keys.DefaultWebhookId]
            ?.takeIf { id -> webhooks.any { it.id == id } }
            ?: webhooks.firstOrNull()?.id.takeIf { storedWebhooks == null }

        AppSettings(
            webhooks = webhooks,
            defaultWebhookId = defaultWebhookId,
            merchantWebhookIds = WebhookSettingsCodec.decodeMerchantWebhookIds(
                preferences[Keys.MerchantWebhookIdsJson],
            ),
            selectedMerchantIds = preferences[Keys.SelectedMerchantIds].orEmpty(),
            qrisHookActive = preferences[Keys.QrisHookActive]
                ?: preferences[Keys.LegacyWatcherEnabled]
                ?: true,
            debugModeEnabled = preferences[Keys.DebugModeEnabled] ?: false,
            debugWatchedPackages = preferences[Keys.DebugWatchedPackages].orEmpty(),
        )
    }

    suspend fun updateSettings(settings: AppSettings) {
        dataStore.edit { preferences ->
            preferences[Keys.WebhooksJson] = WebhookSettingsCodec.encodeWebhooks(settings.webhooks)
            if (settings.defaultWebhookId != null) {
                preferences[Keys.DefaultWebhookId] = settings.defaultWebhookId
            } else {
                preferences.remove(Keys.DefaultWebhookId)
            }
            preferences[Keys.MerchantWebhookIdsJson] =
                WebhookSettingsCodec.encodeMerchantWebhookIds(settings.merchantWebhookIds)
            preferences.remove(Keys.LegacyWebhookUrl)
            preferences.remove(Keys.LegacySecret)
            preferences[Keys.SelectedMerchantIds] = settings.selectedMerchantIds
            preferences[Keys.QrisHookActive] = settings.qrisHookActive
            preferences[Keys.DebugModeEnabled] = settings.debugModeEnabled
            preferences[Keys.DebugWatchedPackages] = settings.debugWatchedPackages
        }
    }

    private object Keys {
        val WebhooksJson = stringPreferencesKey("webhooks_json")
        val DefaultWebhookId = stringPreferencesKey("default_webhook_id")
        val MerchantWebhookIdsJson = stringPreferencesKey("merchant_webhook_ids_json")
        val LegacyWebhookUrl = stringPreferencesKey("webhook_url")
        val LegacySecret = stringPreferencesKey("secret")
        val SelectedMerchantIds = stringSetPreferencesKey("selected_merchant_ids")
        val QrisHookActive = booleanPreferencesKey("qris_hook_active")
        val LegacyWatcherEnabled = booleanPreferencesKey("watcher_enabled")
        val DebugModeEnabled = booleanPreferencesKey("debug_mode_enabled")
        val DebugWatchedPackages = stringSetPreferencesKey("debug_watched_packages")
    }

    private companion object {
        const val LEGACY_WEBHOOK_NAME = "Default"
    }
}
