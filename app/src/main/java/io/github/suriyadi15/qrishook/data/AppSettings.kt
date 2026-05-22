package io.github.suriyadi15.qrishook.data

data class AppSettings(
    val webhookUrl: String = "",
    val secret: String = "",
    val selectedMerchantIds: Set<String> = emptySet(),
    val qrisHookActive: Boolean = true,
    val debugModeEnabled: Boolean = false,
    val debugWatchedPackages: Set<String> = emptySet(),
)
