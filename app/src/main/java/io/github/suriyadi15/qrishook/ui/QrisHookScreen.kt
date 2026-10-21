package io.github.suriyadi15.qrishook.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.os.LocaleListCompat
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import io.github.suriyadi15.qrishook.InstalledAppInfo
import io.github.suriyadi15.qrishook.MainUiState
import io.github.suriyadi15.qrishook.R
import io.github.suriyadi15.qrishook.ParserRequestState
import io.github.suriyadi15.qrishook.data.AppSettings
import io.github.suriyadi15.qrishook.data.DebugNotificationEntity
import io.github.suriyadi15.qrishook.data.EventEntity
import io.github.suriyadi15.qrishook.data.WebhookEndpoint
import io.github.suriyadi15.qrishook.domain.DeliveryStatus
import io.github.suriyadi15.qrishook.issue.MerchantParserRequestResult
import io.github.suriyadi15.qrishook.issue.PayloadRedactor
import io.github.suriyadi15.qrishook.update.AppUpdateState
import io.github.suriyadi15.qrishook.webhook.WebhookPayloadBuilder
import org.json.JSONObject
import java.text.DateFormat
import java.util.Date

private const val PRIVACY_POLICY_URL = "https://qrishook.pages.dev/docs/privacy/"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrisHookScreen(
    state: MainUiState,
    events: LazyPagingItems<EventEntity>,
    debugLogs: LazyPagingItems<DebugNotificationEntity>,
    onSettingsChange: (AppSettings) -> Unit,
    onHistorySearchChange: (String) -> Unit,
    onDebugSearchChange: (String) -> Unit,
    onOpenNotificationAccess: () -> Unit,
    onOpenAppInfo: () -> Unit,
    onRequestIgnoreBatteryOptimizations: () -> Unit,
    onOpenGitHub: () -> Unit,
    onSubmitMerchantParserRequest: (DebugNotificationEntity, String, String, String) -> Unit,
    onResetMerchantParserRequest: () -> Unit,
    onCheckForUpdates: () -> Unit,
    onOpenUpdate: (String) -> Unit,
    onTestDelivery: () -> Unit,
    onTestWebhook: (WebhookEndpoint) -> Unit,
    onClearDebugLogs: () -> Unit,
) {
    var selectedDestination by rememberSaveable { mutableStateOf(MainDestination.Home) }
    var selectedDebugLog by remember { mutableStateOf<DebugNotificationEntity?>(null) }
    var selectedEvent by remember { mutableStateOf<EventEntity?>(null) }
    var requestParserLog by remember { mutableStateOf<DebugNotificationEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.app_name), fontWeight = FontWeight.SemiBold)
                        Text(
                            stringResource(selectedDestination.subtitleRes),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            NavigationBar {
                MainDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = selectedDestination == destination,
                        onClick = { selectedDestination = destination },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = stringResource(destination.labelRes),
                            )
                        },
                        label = { Text(stringResource(destination.labelRes)) },
                    )
                }
            }
        },
    ) { padding ->
        when (selectedDestination) {
            MainDestination.Home -> HomeScreen(
                state = state,
                contentPadding = padding,
                onSettingsChange = onSettingsChange,
                onOpenNotificationAccess = onOpenNotificationAccess,
                onOpenAppInfo = onOpenAppInfo,
                onRequestIgnoreBatteryOptimizations = onRequestIgnoreBatteryOptimizations,
                onOpenGitHub = onOpenGitHub,
                onCheckForUpdates = onCheckForUpdates,
                onOpenUpdate = onOpenUpdate,
                onTestDelivery = onTestDelivery,
                onTestWebhook = onTestWebhook,
            )

            MainDestination.History -> HistoryScreen(
                searchQuery = state.historySearchQuery,
                events = events,
                contentPadding = padding,
                onSearchChange = onHistorySearchChange,
                onEventClick = { selectedEvent = it },
            )

            MainDestination.Debug -> DebugModeScreen(
                state = state,
                debugLogs = debugLogs,
                contentPadding = padding,
                onSettingsChange = onSettingsChange,
                onSearchChange = onDebugSearchChange,
                onDebugLogClick = { selectedDebugLog = it },
                onClearDebugLogs = onClearDebugLogs,
            )
        }
    }

    selectedDebugLog?.let { log ->
        DebugPayloadDialog(
            log = log,
            onDismiss = { selectedDebugLog = null },
            onRequestParser = {
                selectedDebugLog = null
                requestParserLog = log
            },
        )
    }

    requestParserLog?.let { log ->
        MerchantRequestDialog(
            log = log,
            state = state.parserRequestState,
            onSubmit = { expected, contact, payload ->
                onSubmitMerchantParserRequest(log, expected, contact, payload)
            },
            onDismiss = {
                requestParserLog = null
                onResetMerchantParserRequest()
            },
        )
    }

    selectedEvent?.let { event ->
        EventDetailDialog(
            event = event,
            onDismiss = { selectedEvent = null },
        )
    }
}

private enum class MainDestination(
    @StringRes val labelRes: Int,
    @StringRes val subtitleRes: Int,
    val icon: ImageVector,
) {
    Home(
        labelRes = R.string.nav_home,
        subtitleRes = R.string.subtitle_home,
        icon = Icons.Filled.Home,
    ),
    History(
        labelRes = R.string.nav_history,
        subtitleRes = R.string.subtitle_history,
        icon = Icons.Filled.Search,
    ),
    Debug(
        labelRes = R.string.nav_debug,
        subtitleRes = R.string.subtitle_debug,
        icon = Icons.Filled.Settings,
    ),
}

@Composable
private fun HomeScreen(
    state: MainUiState,
    contentPadding: PaddingValues,
    onSettingsChange: (AppSettings) -> Unit,
    onOpenNotificationAccess: () -> Unit,
    onOpenAppInfo: () -> Unit,
    onRequestIgnoreBatteryOptimizations: () -> Unit,
    onOpenGitHub: () -> Unit,
    onCheckForUpdates: () -> Unit,
    onOpenUpdate: (String) -> Unit,
    onTestDelivery: () -> Unit,
    onTestWebhook: (WebhookEndpoint) -> Unit,
) {
    var editorTarget by remember { mutableStateOf<WebhookEditorTarget?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            AccessSection(
                granted = state.notificationAccessGranted,
                onOpenNotificationAccess = onOpenNotificationAccess,
                onOpenAppInfo = onOpenAppInfo,
            )
        }
        item {
            QrisHookActiveSection(
                settings = state.settings,
                notificationAccessGranted = state.notificationAccessGranted,
                onSettingsChange = onSettingsChange,
            )
        }
        item {
            BatteryOptimizationSection(
                ignoringBatteryOptimizations = state.ignoringBatteryOptimizations,
                onRequestIgnoreBatteryOptimizations = onRequestIgnoreBatteryOptimizations,
            )
        }
        item {
            WebhookSection(
                settings = state.settings,
                tests = state.webhookTests,
                onAddWebhook = { editorTarget = WebhookEditorTarget.forNew() },
                onEditWebhook = { editorTarget = WebhookEditorTarget.forId(state.settings, it) },
                onRetryPending = onTestDelivery,
            )
        }
        item {
            MerchantSection(
                settings = state.settings,
                onSettingsChange = onSettingsChange,
                onEditWebhook = { editorTarget = WebhookEditorTarget.forId(state.settings, it) },
            )
        }
        item {
            LanguageSection()
        }
        item {
            OpenSourceSection(onOpenGitHub = onOpenGitHub)
        }
        item {
            AppVersionSection(
                versionName = state.appVersionName,
                versionCode = state.appVersionCode,
                updateState = state.updateState,
                onCheckForUpdates = onCheckForUpdates,
                onOpenUpdate = onOpenUpdate,
            )
        }
    }

    editorTarget?.let { target ->
        WebhookEditorSheet(
            settings = state.settings,
            target = target,
            testState = state.webhookTests[target.endpoint.id],
            onSettingsChange = onSettingsChange,
            onTest = onTestWebhook,
            onDismiss = { editorTarget = null },
        )
    }
}

@Composable
private fun HistoryScreen(
    searchQuery: String,
    events: LazyPagingItems<EventEntity>,
    contentPadding: PaddingValues,
    onSearchChange: (String) -> Unit,
    onEventClick: (EventEntity) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SearchField(
                value = searchQuery,
                onValueChange = onSearchChange,
                label = stringResource(R.string.search_history),
            )
        }

        when {
            events.loadState.refresh is LoadState.Loading -> {
                item { EmptyState(stringResource(R.string.history_loading)) }
            }

            events.loadState.refresh is LoadState.Error -> {
                item { EmptyState(stringResource(R.string.history_error)) }
            }

            events.itemCount == 0 -> {
                item { EmptyState(stringResource(R.string.history_empty)) }
            }

            else -> {
                items(
                    count = events.itemCount,
                    key = { index -> events[index]?.eventId ?: "event-placeholder-$index" },
                ) { index ->
                    events[index]?.let { event ->
                        EventRow(
                            event = event,
                            onClick = { onEventClick(event) },
                        )
                    }
                }
            }
        }

        item {
            AppendState(
                loadState = events.loadState.append,
                loadingText = stringResource(R.string.history_loading_more),
                errorText = stringResource(R.string.history_error_more),
            )
        }
    }
}

@Composable
private fun DebugModeScreen(
    state: MainUiState,
    debugLogs: LazyPagingItems<DebugNotificationEntity>,
    contentPadding: PaddingValues,
    onSettingsChange: (AppSettings) -> Unit,
    onSearchChange: (String) -> Unit,
    onDebugLogClick: (DebugNotificationEntity) -> Unit,
    onClearDebugLogs: () -> Unit,
) {
    var showAppPicker by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            DebugSettingsSection(
                settings = state.settings,
                onSettingsChange = onSettingsChange,
                onPickApps = { showAppPicker = true },
            )
        }
        item {
            MerchantParserContributionSection()
        }
        item {
            DebugLogHeader(
                onClearDebugLogs = onClearDebugLogs,
            )
        }
        item {
            SearchField(
                value = state.debugSearchQuery,
                onValueChange = onSearchChange,
                label = stringResource(R.string.search_debug_logs),
            )
        }

        when {
            debugLogs.loadState.refresh is LoadState.Loading -> {
                item { EmptyState(stringResource(R.string.debug_loading)) }
            }

            debugLogs.loadState.refresh is LoadState.Error -> {
                item { EmptyState(stringResource(R.string.debug_error)) }
            }

            debugLogs.itemCount == 0 -> {
                item { EmptyState(stringResource(R.string.debug_empty)) }
            }

            else -> {
                items(
                    count = debugLogs.itemCount,
                    key = { index -> debugLogs[index]?.id ?: "debug-placeholder-$index" },
                ) { index ->
                    debugLogs[index]?.let { log ->
                        DebugLogRow(
                            log = log,
                            onClick = { onDebugLogClick(log) },
                        )
                    }
                }
            }
        }

        item {
            AppendState(
                loadState = debugLogs.loadState.append,
                loadingText = stringResource(R.string.debug_loading_more),
                errorText = stringResource(R.string.debug_error_more),
            )
        }
    }

    if (showAppPicker) {
        AppPickerDialog(
            apps = state.installedApps,
            selectedPackages = state.settings.debugWatchedPackages,
            onSelectedPackagesChange = {
                onSettingsChange(state.settings.copy(debugWatchedPackages = it))
            },
            onDismiss = { showAppPicker = false },
        )
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun AppendState(
    loadState: LoadState,
    loadingText: String,
    errorText: String,
) {
    when (loadState) {
        is LoadState.Loading -> EmptyState(loadingText)
        is LoadState.Error -> EmptyState(errorText)
        is LoadState.NotLoading -> Unit
    }
}

@Composable
private fun AccessSection(
    granted: Boolean,
    onOpenNotificationAccess: () -> Unit,
    onOpenAppInfo: () -> Unit,
) {
    Section {
        Text(stringResource(R.string.access_title), fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(if (granted) R.string.access_enabled else R.string.access_disabled),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
        )
        if (!granted) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.access_restricted_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(onClick = onOpenNotificationAccess) {
                Text(stringResource(if (granted) R.string.access_open else R.string.access_enable))
            }
            OutlinedButton(onClick = onOpenAppInfo) {
                Text(stringResource(R.string.app_info))
            }
        }
    }
}

@Composable
private fun BatteryOptimizationSection(
    ignoringBatteryOptimizations: Boolean,
    onRequestIgnoreBatteryOptimizations: () -> Unit,
) {
    Section {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
            ) {
                Text(stringResource(R.string.battery_title), fontWeight = FontWeight.SemiBold)
                Text(
                    stringResource(
                        if (ignoringBatteryOptimizations) R.string.battery_unrestricted else R.string.battery_restricted,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            Button(
                enabled = !ignoringBatteryOptimizations,
                onClick = onRequestIgnoreBatteryOptimizations,
            ) {
                Text(stringResource(if (ignoringBatteryOptimizations) R.string.battery_set else R.string.battery_allow))
            }
        }
    }
}

@Composable
private fun QrisHookActiveSection(
    settings: AppSettings,
    notificationAccessGranted: Boolean,
    onSettingsChange: (AppSettings) -> Unit,
) {
    Section {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.active_title), fontWeight = FontWeight.SemiBold)
                Text(
                    stringResource(
                        when {
                            !settings.qrisHookActive -> R.string.active_paused
                            !notificationAccessGranted -> R.string.active_waiting
                            else -> R.string.active_running
                        },
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            Switch(
                checked = settings.qrisHookActive,
                onCheckedChange = {
                    onSettingsChange(settings.copy(qrisHookActive = it))
                },
            )
        }
    }
}

@Composable
private fun OpenSourceSection(
    onOpenGitHub: () -> Unit,
) {
    Section {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
            ) {
                Text(stringResource(R.string.opensource_title), fontWeight = FontWeight.SemiBold)
                Text(
                    stringResource(R.string.opensource_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            OutlinedButton(onClick = onOpenGitHub) {
                Text(stringResource(R.string.github))
            }
        }
    }
}

@Composable
private fun AppVersionSection(
    versionName: String,
    versionCode: Int,
    updateState: AppUpdateState,
    onCheckForUpdates: () -> Unit,
    onOpenUpdate: (String) -> Unit,
) {
    Section {
        Text(stringResource(R.string.version_title), fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(
            stringResource(R.string.version_line, versionName, versionCode),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = updateStateText(updateState),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                enabled = updateState !is AppUpdateState.Loading,
                onClick = onCheckForUpdates,
            ) {
                Text(stringResource(if (updateState is AppUpdateState.Loading) R.string.checking else R.string.check_update))
            }

            if (updateState is AppUpdateState.UpdateAvailable) {
                Button(onClick = { onOpenUpdate(updateState.downloadUrl) }) {
                    Text(stringResource(R.string.update))
                }
            }
        }
    }
}

@Composable
private fun updateStateText(updateState: AppUpdateState): String {
    return when (updateState) {
        AppUpdateState.Idle -> stringResource(R.string.update_idle)
        AppUpdateState.Loading -> stringResource(R.string.update_loading)
        is AppUpdateState.UpToDate -> stringResource(R.string.update_up_to_date, updateState.latestVersion)
        is AppUpdateState.UpdateAvailable -> stringResource(R.string.update_available, updateState.latestVersion)
        is AppUpdateState.Error -> {
            val detail = when (updateState.reason) {
                AppUpdateState.Error.Reason.Http ->
                    stringResource(R.string.update_err_http, updateState.detail.toIntOrNull() ?: 0)
                AppUpdateState.Error.Reason.Unreadable -> stringResource(R.string.update_err_read)
                AppUpdateState.Error.Reason.Other -> updateState.detail
            }
            stringResource(R.string.update_failed, detail)
        }
    }
}

@Composable
private fun parserRequestErrorText(failure: MerchantParserRequestResult.Failure): String {
    return when (failure.reason) {
        MerchantParserRequestResult.Failure.Reason.UnexpectedResponse ->
            stringResource(R.string.request_err_unexpected)
        MerchantParserRequestResult.Failure.Reason.ServerMessage -> failure.detail
        MerchantParserRequestResult.Failure.Reason.ServerHttp ->
            stringResource(R.string.request_err_http, failure.detail.toIntOrNull() ?: 0)
        MerchantParserRequestResult.Failure.Reason.Unreachable ->
            stringResource(R.string.request_err_unreachable)
    }
}

@Composable
private fun LanguageSection() {
    // AppCompat stores and restores the choice; "" means follow the system language.
    var selected by remember { mutableStateOf(currentLanguageTag()) }
    val options = listOf(
        "" to R.string.language_system,
        "en" to R.string.language_english,
        "id" to R.string.language_indonesian,
    )

    Section {
        Text(stringResource(R.string.language_title), fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        options.forEach { (tag, labelRes) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        selected = tag
                        AppCompatDelegate.setApplicationLocales(
                            if (tag.isEmpty()) {
                                LocaleListCompat.getEmptyLocaleList()
                            } else {
                                LocaleListCompat.forLanguageTags(tag)
                            },
                        )
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(selected = selected == tag, onClick = null)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(labelRes), modifier = Modifier.padding(vertical = 10.dp))
            }
        }
    }
}

private fun currentLanguageTag(): String {
    val tag = AppCompatDelegate.getApplicationLocales().toLanguageTags()
    return when {
        tag.startsWith("id") || tag.startsWith("in") -> "id"
        tag.startsWith("en") -> "en"
        else -> ""
    }
}

@Composable
private fun DebugSettingsSection(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onPickApps: () -> Unit,
) {
    Section {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.debug_mode_title), fontWeight = FontWeight.SemiBold)
                Text(
                    stringResource(
                        when {
                            !settings.debugModeEnabled -> R.string.debug_mode_off
                            settings.debugWatchedPackages.isEmpty() -> R.string.debug_mode_active_no_apps
                            else -> R.string.debug_mode_active
                        },
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            Switch(
                checked = settings.debugModeEnabled,
                onCheckedChange = {
                    onSettingsChange(settings.copy(debugModeEnabled = it))
                },
            )
        }

        if (settings.debugModeEnabled) {
            Spacer(Modifier.height(12.dp))
            Text(
                pluralStringResource(
                    R.plurals.apps_selected,
                    settings.debugWatchedPackages.size,
                    settings.debugWatchedPackages.size,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onPickApps) {
                Text(stringResource(R.string.select_apps))
            }
        }
    }
}

@Composable
private fun MerchantParserContributionSection() {
    Section {
        Text(stringResource(R.string.request_support_title), fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.request_support_p1),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.request_support_p2),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.request_support_p3),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun DebugLogHeader(
    onClearDebugLogs: () -> Unit,
) {
    Section {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.debug_logs_title), fontWeight = FontWeight.SemiBold)
                Text(
                    stringResource(R.string.subtitle_debug),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            OutlinedButton(
                onClick = onClearDebugLogs,
            ) {
                Text(stringResource(R.string.clear))
            }
        }
    }
}

@Composable
private fun AppPickerDialog(
    apps: List<InstalledAppInfo>,
    selectedPackages: Set<String>,
    onSelectedPackagesChange: (Set<String>) -> Unit,
    onDismiss: () -> Unit,
) {
    var appSearch by remember { mutableStateOf("") }
    val query = appSearch.trim()
    val filteredApps = remember(apps, query) {
        if (query.isBlank()) {
            apps
        } else {
            apps.filter { app ->
                app.label.contains(query, ignoreCase = true) ||
                    app.packageName.contains(query, ignoreCase = true)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_apps)) },
        text = {
            Column {
                OutlinedTextField(
                    value = appSearch,
                    onValueChange = { appSearch = it },
                    label = { Text(stringResource(R.string.search_apps)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    pluralStringResource(
                        R.plurals.apps_selected,
                        selectedPackages.size,
                        selectedPackages.size,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(Modifier.height(8.dp))
                if (filteredApps.isEmpty()) {
                    EmptyState(stringResource(R.string.no_matching_apps))
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            AppPickerRow(
                                app = app,
                                checked = app.packageName in selectedPackages,
                                onCheckedChange = { isChecked ->
                                    val next = if (isChecked) {
                                        selectedPackages + app.packageName
                                    } else {
                                        selectedPackages - app.packageName
                                    }
                                    onSelectedPackagesChange(next)
                                },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.done))
            }
        },
    )
}

@Composable
private fun AppPickerRow(
    app: InstalledAppInfo,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(app.label, fontWeight = FontWeight.Medium)
            Text(
                app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun DebugLogRow(
    log: DebugNotificationEntity,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(log.sourceApp, fontWeight = FontWeight.Medium)
            Text(
                log.sourcePackage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                log.title.ifBlank { log.text }.ifBlank { formatMillis(log.capturedAtMillis) },
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun MerchantRequestDialog(
    log: DebugNotificationEntity,
    state: ParserRequestState,
    onSubmit: (expectedDetails: String, contact: String, debugPayload: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    var expectedDetails by rememberSaveable { mutableStateOf("") }
    var contact by rememberSaveable { mutableStateOf("") }
    var payload by rememberSaveable { mutableStateOf(PayloadRedactor.redact(log.payloadJson)) }
    var confirmed by rememberSaveable { mutableStateOf(false) }
    val payloadValid = remember(payload) { runCatching { JSONObject(payload) }.isSuccess }
    val sending = state is ParserRequestState.Sending
    val sent = state is ParserRequestState.Sent

    AlertDialog(
        onDismissRequest = { if (!sending) onDismiss() },
        title = { Text(stringResource(R.string.request_dialog_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (state is ParserRequestState.Sent) {
                    Text(stringResource(R.string.request_sent, state.id))
                } else {
                    Text(log.sourceApp, fontWeight = FontWeight.Medium)
                    Text(
                        log.sourcePackage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    OutlinedTextField(
                        value = expectedDetails,
                        onValueChange = { expectedDetails = it },
                        enabled = !sending,
                        label = { Text(stringResource(R.string.expected_details_label)) },
                        placeholder = { Text(stringResource(R.string.expected_details_placeholder)) },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = contact,
                        onValueChange = { contact = it },
                        enabled = !sending,
                        label = { Text(stringResource(R.string.contact_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = payload,
                        onValueChange = { payload = it },
                        enabled = !sending,
                        label = { Text(stringResource(R.string.debug_payload_label)) },
                        isError = !payloadValid,
                        supportingText = {
                            Text(
                                stringResource(
                                    if (payloadValid) R.string.payload_masked_hint else R.string.payload_invalid,
                                ),
                            )
                        },
                        textStyle = MaterialTheme.typography.bodySmall,
                        maxLines = 10,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(enabled = !sending) { confirmed = !confirmed },
                    ) {
                        Checkbox(checked = confirmed, onCheckedChange = null, enabled = !sending)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.request_consent),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    TextButton(onClick = { uriHandler.openUri(PRIVACY_POLICY_URL) }) {
                        Text(stringResource(R.string.read_privacy))
                    }
                    if (state is ParserRequestState.Error) {
                        Text(
                            parserRequestErrorText(state.failure),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (sent) {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
            } else {
                TextButton(
                    onClick = { onSubmit(expectedDetails, contact, payload) },
                    enabled = confirmed && payloadValid && !sending,
                ) {
                    Text(stringResource(if (sending) R.string.sending else R.string.send))
                }
            }
        },
        dismissButton = {
            if (!sent) {
                TextButton(onClick = onDismiss, enabled = !sending) { Text(stringResource(R.string.cancel)) }
            }
        },
    )
}

@Composable
private fun DebugPayloadDialog(
    log: DebugNotificationEntity,
    onDismiss: () -> Unit,
    onRequestParser: () -> Unit,
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(log.sourceApp) },
        text = {
            Text(
                text = log.payloadJson,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .verticalScroll(rememberScrollState()),
                style = MaterialTheme.typography.bodySmall,
            )
        },
        confirmButton = {
            Row {
                TextButton(onClick = onRequestParser) {
                    Text(stringResource(R.string.request_support_button))
                }
                TextButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE)
                            as ClipboardManager
                        clipboard.setPrimaryClip(
                            ClipData.newPlainText("Notification payload", log.payloadJson),
                        )
                    },
                ) {
                    Text(stringResource(R.string.copy))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
    )
}

@Composable
private fun EventRow(
    event: EventEntity,
    onClick: () -> Unit,
) {
    Section(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Rp${event.amount}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = event.sourceApp,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                val paymentDetail = listOfNotNull(event.senderName, event.paymentSource)
                    .joinToString(separator = " - ")
                if (paymentDetail.isNotBlank()) {
                    Text(
                        text = paymentDetail,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(
                    text = event.title.ifBlank { event.text },
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Text(
                text = event.deliverySummary(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun EventDetailDialog(
    event: EventEntity,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val detailJson = remember(event) { eventDetailJson(event) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(event.sourceApp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DetailSection(stringResource(R.string.detail_payment)) {
                    DetailLine(stringResource(R.string.detail_amount), "Rp${event.amount}")
                    DetailLine(stringResource(R.string.detail_merchant), event.merchantId)
                    DetailLine(stringResource(R.string.detail_sender), event.senderName.orEmpty().ifBlank { "-" })
                    DetailLine(stringResource(R.string.detail_source), event.paymentSource.orEmpty().ifBlank { "-" })
                    DetailLine(stringResource(R.string.detail_received), event.receivedAt)
                }

                DetailSection(stringResource(R.string.detail_delivery)) {
                    DetailLine(stringResource(R.string.detail_status), event.status.label())
                    DetailLine(stringResource(R.string.detail_attempts), event.attempts.toString())
                    DetailLine(stringResource(R.string.detail_last_error), event.lastError.ifBlank { "-" })
                    DetailLine(
                        stringResource(R.string.detail_last_attempt),
                        event.lastWebhookAttemptAtMillis?.let(::formatMillis) ?: "-",
                    )
                    DetailLine(stringResource(R.string.detail_updated), formatMillis(event.updatedAtMillis))
                }

                DetailSection(stringResource(R.string.detail_webhook_response)) {
                    DetailLine(stringResource(R.string.detail_webhook_target), event.lastWebhookUrl.ifBlank { "-" })
                    DetailLine(stringResource(R.string.detail_code), event.lastResponseCode?.toString() ?: "-")
                    DetailLine(stringResource(R.string.detail_message), event.lastResponseMessage.ifBlank { "-" })
                    DetailBlock(stringResource(R.string.detail_body), event.lastResponseBody.ifBlank { "-" })
                }

                DetailSection(stringResource(R.string.detail_notification)) {
                    DetailLine(stringResource(R.string.detail_package), event.sourcePackage)
                    DetailLine(stringResource(R.string.detail_title), event.title.ifBlank { "-" })
                    DetailBlock(stringResource(R.string.detail_text), event.text.ifBlank { "-" })
                    DetailBlock(stringResource(R.string.detail_big_text), event.bigText.ifBlank { "-" })
                }

                DetailSection(stringResource(R.string.detail_webhook_payload)) {
                    DetailBlock("JSON", WebhookPayloadBuilder.build(event))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE)
                        as ClipboardManager
                    clipboard.setPrimaryClip(
                        ClipData.newPlainText("QRIS event detail", detailJson),
                    )
                },
            ) {
                Text(stringResource(R.string.copy))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
    )
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold)
        content()
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun DetailBlock(label: String, value: String) {
    DetailLine(label, value)
}

@Composable
private fun DeliveryStatus.label(): String {
    return stringResource(
        when (this) {
            DeliveryStatus.Pending -> R.string.status_pending
            DeliveryStatus.Sending -> R.string.status_sending
            DeliveryStatus.Sent -> R.string.status_sent
            DeliveryStatus.Failed -> R.string.status_failed
        },
    )
}

@Composable
private fun EventEntity.deliverySummary(): String {
    val label = status.label()
    return lastResponseCode?.let { "$label $it" } ?: label
}

private fun formatMillis(millis: Long): String {
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM)
        .format(Date(millis))
}

private fun eventDetailJson(event: EventEntity): String {
    return jsonObject(
        listOf(
            jsonField("event_id", event.eventId),
            jsonField("type", event.type),
            jsonField("merchant_id", event.merchantId),
            jsonObjectField(
                "payment",
                listOf(
                    jsonField("amount", event.amount),
                    jsonField("currency", event.currency),
                    jsonNullableField("sender_name", event.senderName),
                    jsonNullableField("payment_source", event.paymentSource),
                ),
            ),
            jsonObjectField(
                "notification",
                listOf(
                    jsonField("source_package", event.sourcePackage),
                    jsonField("source_app", event.sourceApp),
                    jsonField("title", event.title),
                    jsonField("text", event.text),
                    jsonField("big_text", event.bigText),
                    jsonField("received_at", event.receivedAt),
                ),
            ),
            jsonObjectField(
                "delivery",
                listOf(
                    jsonField("status", event.status.name),
                    jsonField("attempts", event.attempts.toLong()),
                    jsonField("last_error", event.lastError),
                    jsonNullableField("last_webhook_attempt_at_millis", event.lastWebhookAttemptAtMillis),
                    jsonField("created_at_millis", event.createdAtMillis),
                    jsonField("updated_at_millis", event.updatedAtMillis),
                ),
            ),
            jsonObjectField(
                "webhook_response",
                listOf(
                    jsonNullableField("code", event.lastResponseCode?.toLong()),
                    jsonField("message", event.lastResponseMessage),
                    jsonField("body", event.lastResponseBody),
                ),
            ),
            "\"webhook_payload\":${WebhookPayloadBuilder.build(event)}",
        ),
    )
}

private fun jsonObject(fields: List<String>): String {
    return fields.joinToString(separator = ",", prefix = "{", postfix = "}")
}

private fun jsonObjectField(name: String, fields: List<String>): String {
    return "${jsonQuote(name)}:${jsonObject(fields)}"
}

private fun jsonField(name: String, value: String): String {
    return "${jsonQuote(name)}:${jsonQuote(value)}"
}

private fun jsonField(name: String, value: Long): String {
    return "${jsonQuote(name)}:$value"
}

private fun jsonNullableField(name: String, value: String?): String {
    return if (value == null) {
        "${jsonQuote(name)}:null"
    } else {
        jsonField(name, value)
    }
}

private fun jsonNullableField(name: String, value: Long?): String {
    return if (value == null) {
        "${jsonQuote(name)}:null"
    } else {
        jsonField(name, value)
    }
}

private fun jsonQuote(value: String): String {
    return buildString {
        append('"')
        value.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> {
                    if (char.code < 0x20) {
                        append("\\u")
                        append(char.code.toString(16).padStart(4, '0'))
                    } else {
                        append(char)
                    }
                }
            }
        }
        append('"')
    }
}

@Composable
private fun EmptyState(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.medium,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
internal fun Section(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}
