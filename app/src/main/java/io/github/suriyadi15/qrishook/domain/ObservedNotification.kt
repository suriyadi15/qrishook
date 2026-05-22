package io.github.suriyadi15.qrishook.domain

data class ObservedNotification(
    val sourcePackage: String,
    val sourceApp: String,
    val title: String,
    val text: String,
    val bigText: String,
    val postedAtMillis: Long,
) {
    val combinedText: String
        get() = listOf(title, text, bigText)
            .filter { it.isNotBlank() }
            .joinToString(separator = "\n")
}
