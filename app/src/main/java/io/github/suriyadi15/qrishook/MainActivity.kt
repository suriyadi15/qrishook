package io.github.suriyadi15.qrishook

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.suriyadi15.qrishook.notification.NotificationAccessHelper
import io.github.suriyadi15.qrishook.ui.QrisHookScreen
import io.github.suriyadi15.qrishook.ui.QrisHookTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        viewModel.syncQrisHookActive(isNotificationAccessGranted())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()
        setContent {
            QrisHookTheme {
                val state by viewModel.uiState.collectAsState()
                val events = viewModel.pagedEvents.collectAsLazyPagingItems()
                val debugLogs = viewModel.pagedDebugLogs.collectAsLazyPagingItems()
                QrisHookScreen(
                    state = state.copy(
                        notificationAccessGranted = isNotificationAccessGranted(),
                    ),
                    events = events,
                    debugLogs = debugLogs,
                    onSettingsChange = viewModel::updateSettings,
                    onHistorySearchChange = viewModel::updateHistorySearchQuery,
                    onDebugSearchChange = viewModel::updateDebugSearchQuery,
                    onOpenNotificationAccess = {
                        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    },
                    onOpenGitHub = {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PROJECT_URL)))
                    },
                    onTestDelivery = viewModel::enqueueDelivery,
                    onClearDebugLogs = viewModel::clearDebugLogs,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.syncQrisHookActive(isNotificationAccessGranted())
        viewModel.refresh()
    }

    private fun isNotificationAccessGranted(): Boolean {
        return NotificationAccessHelper.isGranted(this)
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
    }
}
