package io.github.suriyadi15.qrishook

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import io.github.suriyadi15.qrishook.data.AppSettings
import io.github.suriyadi15.qrishook.notification.NotificationAccessHelper
import io.github.suriyadi15.qrishook.notification.WatcherForegroundService
import io.github.suriyadi15.qrishook.update.AppUpdateState
import io.github.suriyadi15.qrishook.worker.WebhookDeliveryWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val graph = AppGraph.get(application)
    private val installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    private val historySearchQuery = MutableStateFlow("")
    private val debugSearchQuery = MutableStateFlow("")
    private val updateState = MutableStateFlow<AppUpdateState>(AppUpdateState.Idle)

    init {
        loadInstalledApps()
        checkForUpdates()
    }

    val pagedEvents = historySearchQuery
        .flatMapLatest { query -> graph.eventRepository.pagedEvents(query) }
        .cachedIn(viewModelScope)

    val pagedDebugLogs = debugSearchQuery
        .flatMapLatest { query -> graph.debugNotificationRepository.pagedLogs(query) }
        .cachedIn(viewModelScope)

    val uiState = combine(
        graph.settingsRepository.settings,
        installedApps,
        historySearchQuery,
        debugSearchQuery,
        updateState,
    ) { settings, apps, historyQuery, debugQuery, update ->
        MainUiState(
            settings = settings,
            installedApps = apps,
            historySearchQuery = historyQuery,
            debugSearchQuery = debugQuery,
            updateState = update,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainUiState(),
    )

    fun updateSettings(settings: AppSettings) {
        viewModelScope.launch {
            graph.settingsRepository.updateSettings(settings)
            applyQrisHookActiveState(settings.qrisHookActive && NotificationAccessHelper.isGranted(getApplication()))
        }
    }

    fun updateHistorySearchQuery(query: String) {
        historySearchQuery.value = query
    }

    fun updateDebugSearchQuery(query: String) {
        debugSearchQuery.value = query
    }

    fun enqueueDelivery() {
        WebhookDeliveryWorker.enqueue(getApplication())
    }

    fun clearDebugLogs() {
        viewModelScope.launch {
            graph.debugNotificationRepository.clear()
        }
    }

    fun checkForUpdates() {
        if (updateState.value is AppUpdateState.Loading) return

        viewModelScope.launch(Dispatchers.IO) {
            updateState.value = AppUpdateState.Loading
            updateState.value = graph.updateChecker.check(BuildConfig.VERSION_NAME)
        }
    }

    fun refresh() {
        loadInstalledApps()
    }

    fun syncQrisHookActive(notificationAccessGranted: Boolean) {
        viewModelScope.launch {
            val settings = graph.settingsRepository.settings.first()
            applyQrisHookActiveState(settings.qrisHookActive && notificationAccessGranted)
        }
    }

    private fun applyQrisHookActiveState(shouldRun: Boolean) {
        if (shouldRun) {
            WatcherForegroundService.start(getApplication())
        } else {
            WatcherForegroundService.stop(getApplication())
        }
    }

    private fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val packageManager = getApplication<Application>().packageManager
            val apps = packageManager.installedApplicationsCompat()
                .map { app ->
                    InstalledAppInfo(
                        packageName = app.packageName,
                        label = packageManager.getApplicationLabel(app).toString(),
                    )
                }
                .distinctBy { it.packageName }
                .sortedWith(
                    compareBy<InstalledAppInfo> { it.label.lowercase() }
                        .thenBy { it.packageName },
                )
            installedApps.value = apps
        }
    }

    private fun PackageManager.installedApplicationsCompat(): List<ApplicationInfo> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            getInstalledApplications(0)
        }
    }
}

data class MainUiState(
    val settings: AppSettings = AppSettings(),
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val appVersionName: String = BuildConfig.VERSION_NAME,
    val appVersionCode: Int = BuildConfig.VERSION_CODE,
    val updateState: AppUpdateState = AppUpdateState.Idle,
    val historySearchQuery: String = "",
    val debugSearchQuery: String = "",
    val notificationAccessGranted: Boolean = false,
    val ignoringBatteryOptimizations: Boolean = false,
)

data class InstalledAppInfo(
    val packageName: String,
    val label: String,
)
