package io.github.suriyadi15.qrishook

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.suriyadi15.qrishook.notification.NotificationAccessHelper
import io.github.suriyadi15.qrishook.ui.QrisHookScreen
import io.github.suriyadi15.qrishook.ui.QrisHookTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private var notificationAccessGranted by mutableStateOf(false)
    private var ignoringBatteryOptimizations by mutableStateOf(false)
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        refreshSystemAccessState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshSystemAccessState()
        requestNotificationPermissionIfNeeded()
        setContent {
            QrisHookTheme {
                val state by viewModel.uiState.collectAsState()
                val events = viewModel.pagedEvents.collectAsLazyPagingItems()
                val debugLogs = viewModel.pagedDebugLogs.collectAsLazyPagingItems()
                QrisHookScreen(
                    state = state.copy(
                        notificationAccessGranted = notificationAccessGranted,
                        ignoringBatteryOptimizations = ignoringBatteryOptimizations,
                    ),
                    events = events,
                    debugLogs = debugLogs,
                    onSettingsChange = viewModel::updateSettings,
                    onHistorySearchChange = viewModel::updateHistorySearchQuery,
                    onDebugSearchChange = viewModel::updateDebugSearchQuery,
                    onOpenNotificationAccess = {
                        startActivitySafely(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    },
                    onOpenAppInfo = {
                        startActivitySafely(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(packageUri()),
                        )
                    },
                    onRequestIgnoreBatteryOptimizations = {
                        startActivitySafely(
                            Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).setData(packageUri()),
                        )
                    },
                    onOpenGitHub = {
                        startActivitySafely(Intent(Intent.ACTION_VIEW, Uri.parse(PROJECT_URL)))
                    },
                    onOpenMerchantParserRequest = {
                        startActivitySafely(
                            Intent(Intent.ACTION_VIEW, Uri.parse(MERCHANT_PARSER_REQUEST_URL)),
                        )
                    },
                    onCheckForUpdates = viewModel::checkForUpdates,
                    onOpenUpdate = { url ->
                        startActivitySafely(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    },
                    onTestDelivery = viewModel::enqueueDelivery,
                    onClearDebugLogs = viewModel::clearDebugLogs,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshSystemAccessState()
        viewModel.refresh()
    }

    private fun isNotificationAccessGranted(): Boolean {
        return NotificationAccessHelper.isGranted(this)
    }

    private fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }

    private fun refreshSystemAccessState() {
        notificationAccessGranted = isNotificationAccessGranted()
        ignoringBatteryOptimizations = isIgnoringBatteryOptimizations()
        viewModel.syncQrisHookActive(notificationAccessGranted)
    }

    private fun packageUri(): Uri {
        return Uri.parse("package:$packageName")
    }

    private fun startActivitySafely(intent: Intent) {
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            notificationPermissionLauncher.launch(permission)
        }
    }

    private companion object {
        const val PROJECT_URL = "https://github.com/suriyadi15/qrishook"
        const val MERCHANT_PARSER_REQUEST_URL =
            "https://github.com/suriyadi15/qrishook/issues/new?template=merchant_parser_request.yml"
    }
}
