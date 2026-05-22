package io.github.suriyadi15.qrishook.update

sealed interface AppUpdateState {
    data object Idle : AppUpdateState
    data object Loading : AppUpdateState

    data class UpToDate(
        val latestVersion: String,
    ) : AppUpdateState

    data class UpdateAvailable(
        val latestVersion: String,
        val downloadUrl: String,
    ) : AppUpdateState

    data class Error(
        val message: String,
    ) : AppUpdateState
}
