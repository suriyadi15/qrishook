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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import io.github.suriyadi15.qrishook.InstalledAppInfo
import io.github.suriyadi15.qrishook.MainUiState
import io.github.suriyadi15.qrishook.data.AppSettings
import io.github.suriyadi15.qrishook.data.DebugNotificationEntity
import io.github.suriyadi15.qrishook.data.EventEntity
import io.github.suriyadi15.qrishook.merchant.MerchantRegistry
import io.github.suriyadi15.qrishook.update.AppUpdateState
import io.github.suriyadi15.qrishook.webhook.WebhookPayloadBuilder
import java.text.DateFormat
import java.util.Date

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
    onOpenMerchantParserRequest: () -> Unit,
    onCheckForUpdates: () -> Unit,
    onOpenUpdate: (String) -> Unit,
    onTestDelivery: () -> Unit,
    onClearDebugLogs: () -> Unit,
) {
    var selectedDestination by rememberSaveable { mutableStateOf(MainDestination.Home) }
    var selectedDebugLog by remember { mutableStateOf<DebugNotificationEntity?>(null) }
    var selectedEvent by remember { mutableStateOf<EventEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("QRIS Hook", fontWeight = FontWeight.SemiBold)
                        Text(
                            selectedDestination.subtitle,
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
                                contentDescription = destination.label,
                            )
                        },
                        label = { Text(destination.label) },
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
                onOpenMerchantParserRequest = onOpenMerchantParserRequest,
            )
        }
    }

    selectedDebugLog?.let { log ->
        DebugPayloadDialog(
            log = log,
            onDismiss = { selectedDebugLog = null },
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
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
) {
    Home(
        label = "Home",
        subtitle = "QRIS notification to webhook",
        icon = Icons.Filled.Home,
    ),
    History(
        label = "History",
        subtitle = "Matched QRIS events",
        icon = Icons.Filled.Search,
    ),
    Debug(
        label = "Debug",
        subtitle = "Raw notification payloads",
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
) {
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
                onSettingsChange = onSettingsChange,
                onTestDelivery = onTestDelivery,
            )
        }
        item {
            MerchantSection(
                settings = state.settings,
                onSettingsChange = onSettingsChange,
            )
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
                label = "Search history",
            )
        }

        when {
            events.loadState.refresh is LoadState.Loading -> {
                item { EmptyState("Loading history...") }
            }

            events.loadState.refresh is LoadState.Error -> {
                item { EmptyState("Failed to load history.") }
            }

            events.itemCount == 0 -> {
                item { EmptyState("No matching QRIS events yet.") }
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
                loadingText = "Loading more history...",
                errorText = "Failed to load more history.",
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
    onOpenMerchantParserRequest: () -> Unit,
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
            MerchantParserContributionSection(
                onOpenMerchantParserRequest = onOpenMerchantParserRequest,
            )
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
                label = "Search debug logs",
            )
        }

        when {
            debugLogs.loadState.refresh is LoadState.Loading -> {
                item { EmptyState("Loading debug logs...") }
            }

            debugLogs.loadState.refresh is LoadState.Error -> {
                item { EmptyState("Failed to load debug logs.") }
            }

            debugLogs.itemCount == 0 -> {
                item { EmptyState("No debug payloads yet.") }
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
                loadingText = "Loading more debug logs...",
                errorText = "Failed to load more debug logs.",
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
        Text("Notification Access", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(
            if (granted) "Enabled. QRIS Hook can read notifications."
            else "Not enabled. Allow QRIS Hook in Android settings.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
        )
        if (!granted) {
            Spacer(Modifier.height(8.dp))
            Text(
                "If Android blocks this with restricted settings, open App Info, tap More, then Allow restricted settings.",
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
                Text(if (granted) "Open access" else "Enable access")
            }
            OutlinedButton(onClick = onOpenAppInfo) {
                Text("App info")
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
                Text("Battery", fontWeight = FontWeight.SemiBold)
                Text(
                    if (ignoringBatteryOptimizations) {
                        "No restrictions. Android should not stop QRIS Hook in the background."
                    } else {
                        "Restricted. Set no restrictions so notification monitoring keeps running."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            Button(
                enabled = !ignoringBatteryOptimizations,
                onClick = onRequestIgnoreBatteryOptimizations,
            ) {
                Text(if (ignoringBatteryOptimizations) "Set" else "Allow")
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
                Text("QRIS Hook Active", fontWeight = FontWeight.SemiBold)
                Text(
                    when {
                        !settings.qrisHookActive -> "Paused. Merchant and debug settings stay saved."
                        !notificationAccessGranted -> "Waiting for Notification Access."
                        else -> "Active. QRIS Hook is monitoring notifications in the background."
                    },
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
private fun WebhookSection(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onTestDelivery: () -> Unit,
) {
    var webhookUrl by rememberSaveable { mutableStateOf(settings.webhookUrl) }
    var secret by rememberSaveable { mutableStateOf(settings.secret) }

    LaunchedEffect(settings.webhookUrl, settings.secret) {
        webhookUrl = settings.webhookUrl
        secret = settings.secret
    }

    val hasUnsavedChanges = webhookUrl != settings.webhookUrl || secret != settings.secret

    Section {
        Text("Webhook", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = webhookUrl,
            onValueChange = { webhookUrl = it },
            label = { Text("Webhook URL") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = secret,
            onValueChange = { secret = it },
            label = { Text("Optional secret") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                enabled = hasUnsavedChanges,
                onClick = {
                    onSettingsChange(
                        settings.copy(
                            webhookUrl = webhookUrl,
                            secret = secret,
                        ),
                    )
                },
            ) {
                Text("Save")
            }
            OutlinedButton(onClick = onTestDelivery) {
                Text("Retry pending")
            }
        }
    }
}

@Composable
private fun MerchantSection(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
) {
    Section {
        Text("Merchant", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        MerchantPicker(
            selectedMerchantIds = settings.selectedMerchantIds,
            onSelectedMerchantIdsChange = {
                onSettingsChange(settings.copy(selectedMerchantIds = it))
            },
        )
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
                Text("Open Source", fontWeight = FontWeight.SemiBold)
                Text(
                    "View the source code, report issues, or contribute merchant parsers on GitHub.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            OutlinedButton(onClick = onOpenGitHub) {
                Text("GitHub")
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
        Text("App Version", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(4.dp))
        Text(
            "Version $versionName ($versionCode)",
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
                Text(if (updateState is AppUpdateState.Loading) "Checking..." else "Check update")
            }

            if (updateState is AppUpdateState.UpdateAvailable) {
                Button(onClick = { onOpenUpdate(updateState.downloadUrl) }) {
                    Text("Update")
                }
            }
        }
    }
}

private fun updateStateText(updateState: AppUpdateState): String {
    return when (updateState) {
        AppUpdateState.Idle -> "Update status has not been checked yet."
        AppUpdateState.Loading -> "Checking latest GitHub release..."
        is AppUpdateState.UpToDate -> "You are using the latest version (${updateState.latestVersion})."
        is AppUpdateState.UpdateAvailable -> "Version ${updateState.latestVersion} is available."
        is AppUpdateState.Error -> "Update check failed: ${updateState.message}"
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
                Text("Debug Mode", fontWeight = FontWeight.SemiBold)
                Text(
                    if (settings.debugModeEnabled) {
                        if (settings.debugWatchedPackages.isEmpty()) {
                            "Active, but no apps are selected."
                        } else {
                            "Active. Selected notifications are saved as raw payloads."
                        }
                    } else {
                        "Off. QRIS Hook processes notifications normally."
                    },
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
                "${settings.debugWatchedPackages.size} apps selected",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary,
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onPickApps) {
                Text("Select apps")
            }
        }
    }
}

@Composable
private fun MerchantParserContributionSection(
    onOpenMerchantParserRequest: () -> Unit,
) {
    Section {
        Text("Request a Merchant Parser", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(
            "To support a new merchant, the issue must include a debug payload copied from QRIS Hook.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Turn on Debug Mode, select the merchant app, make a real or test QRIS payment, open the captured debug log, tap Copy, then paste that payload into the GitHub issue.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onOpenMerchantParserRequest) {
            Text("Open parser request")
        }
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
                Text("Debug Logs", fontWeight = FontWeight.SemiBold)
                Text(
                    "Raw notification payloads",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            OutlinedButton(
                onClick = onClearDebugLogs,
            ) {
                Text("Clear")
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
        title = { Text("Select apps") },
        text = {
            Column {
                OutlinedTextField(
                    value = appSearch,
                    onValueChange = { appSearch = it },
                    label = { Text("Search apps") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "${selectedPackages.size} apps selected",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Spacer(Modifier.height(8.dp))
                if (filteredApps.isEmpty()) {
                    EmptyState("No matching apps.")
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
                Text("Done")
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
private fun MerchantPicker(
    selectedMerchantIds: Set<String>,
    onSelectedMerchantIdsChange: (Set<String>) -> Unit,
) {
    val parsers = MerchantRegistry.builtInParsers

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        parsers.forEach { parser ->
            val checked = parser.merchantId in selectedMerchantIds
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(parser.displayName, fontWeight = FontWeight.Medium)
                    Text(
                        parser.merchantPackages.joinToString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                Checkbox(
                    checked = checked,
                    onCheckedChange = { isChecked ->
                        val next = if (isChecked) {
                            selectedMerchantIds + parser.merchantId
                        } else {
                            selectedMerchantIds - parser.merchantId
                        }
                        onSelectedMerchantIdsChange(next)
                    },
                )
            }
        }
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
private fun DebugPayloadDialog(
    log: DebugNotificationEntity,
    onDismiss: () -> Unit,
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
            TextButton(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE)
                        as ClipboardManager
                    clipboard.setPrimaryClip(
                        ClipData.newPlainText("Notification payload", log.payloadJson),
                    )
                },
            ) {
                Text("Copy")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
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
                DetailSection("Payment") {
                    DetailLine("Amount", "Rp${event.amount}")
                    DetailLine("Merchant", event.merchantId)
                    DetailLine("Sender", event.senderName.orEmpty().ifBlank { "-" })
                    DetailLine("Source", event.paymentSource.orEmpty().ifBlank { "-" })
                    DetailLine("Received", event.receivedAt)
                }

                DetailSection("Delivery") {
                    DetailLine("Status", event.status.name)
                    DetailLine("Attempts", event.attempts.toString())
                    DetailLine("Last error", event.lastError.ifBlank { "-" })
                    DetailLine(
                        "Last webhook attempt",
                        event.lastWebhookAttemptAtMillis?.let(::formatMillis) ?: "-",
                    )
                    DetailLine("Updated", formatMillis(event.updatedAtMillis))
                }

                DetailSection("Webhook response") {
                    DetailLine("Code", event.lastResponseCode?.toString() ?: "-")
                    DetailLine("Message", event.lastResponseMessage.ifBlank { "-" })
                    DetailBlock("Body", event.lastResponseBody.ifBlank { "-" })
                }

                DetailSection("Notification") {
                    DetailLine("Package", event.sourcePackage)
                    DetailLine("Title", event.title.ifBlank { "-" })
                    DetailBlock("Text", event.text.ifBlank { "-" })
                    DetailBlock("Big text", event.bigText.ifBlank { "-" })
                }

                DetailSection("Webhook payload") {
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
                Text("Copy")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
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

private fun EventEntity.deliverySummary(): String {
    return lastResponseCode?.let { "${status.name} $it" } ?: status.name
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
private fun Section(
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
