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
        AppSettings(
            webhookUrl = preferences[Keys.WebhookUrl].orEmpty(),
            secret = preferences[Keys.Secret].orEmpty(),
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
            preferences[Keys.WebhookUrl] = settings.webhookUrl.trim()
            preferences[Keys.Secret] = settings.secret.trim()
            preferences[Keys.SelectedMerchantIds] = settings.selectedMerchantIds
            preferences[Keys.QrisHookActive] = settings.qrisHookActive
            preferences[Keys.DebugModeEnabled] = settings.debugModeEnabled
            preferences[Keys.DebugWatchedPackages] = settings.debugWatchedPackages
        }
    }

    private object Keys {
        val WebhookUrl = stringPreferencesKey("webhook_url")
        val Secret = stringPreferencesKey("secret")
        val SelectedMerchantIds = stringSetPreferencesKey("selected_merchant_ids")
        val QrisHookActive = booleanPreferencesKey("qris_hook_active")
        val LegacyWatcherEnabled = booleanPreferencesKey("watcher_enabled")
        val DebugModeEnabled = booleanPreferencesKey("debug_mode_enabled")
        val DebugWatchedPackages = stringSetPreferencesKey("debug_watched_packages")
    }
}
