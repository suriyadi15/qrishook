package io.github.suriyadi15.qrishook.notification

import android.app.Notification
import android.content.pm.PackageManager
import android.os.Bundle
import android.service.notification.StatusBarNotification
import io.github.suriyadi15.qrishook.data.DebugNotificationEntity

object DebugNotificationPayloadBuilder {
    fun build(
        sbn: StatusBarNotification,
        packageManager: PackageManager,
        capturedAtMillis: Long = System.currentTimeMillis(),
    ): DebugNotificationEntity {
        val notification = sbn.notification
        val extras = notification.extras ?: Bundle.EMPTY
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val appName = appName(sbn.packageName, packageManager)

        val payload = linkedMapOf(
            "source_package" to sbn.packageName,
            "source_app" to appName,
            "notification" to linkedMapOf(
                "id" to sbn.id,
                "tag" to sbn.tag,
                "key" to sbn.key,
                "group_key" to sbn.groupKey,
                "override_group_key" to sbn.overrideGroupKey,
                "post_time" to sbn.postTime,
                "captured_at" to capturedAtMillis,
                "category" to notification.category,
                "channel_id" to notification.channelId,
                "priority" to notification.priority,
                "visibility" to notification.visibility,
                "flags" to notification.flags,
                "is_ongoing" to sbn.isOngoing,
                "is_clearable" to sbn.isClearable,
            ),
            "text" to linkedMapOf(
                "title" to title,
                "text" to text,
                "big_text" to bigText,
                "sub_text" to extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString(),
                "info_text" to extras.getCharSequence(Notification.EXTRA_INFO_TEXT)?.toString(),
                "summary_text" to extras.getCharSequence(Notification.EXTRA_SUMMARY_TEXT)?.toString(),
                "text_lines" to extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
                    ?.map { it.toString() },
            ),
            "extras" to extras.toJsonMap(),
        )

        return DebugNotificationEntity(
            sourcePackage = sbn.packageName,
            sourceApp = appName,
            title = title,
            text = text,
            bigText = bigText,
            postedAtMillis = sbn.postTime,
            capturedAtMillis = capturedAtMillis,
            payloadJson = JsonFormatter.format(payload),
        )
    }

    private fun appName(packageName: String, packageManager: PackageManager): String {
        return runCatching {
            val info = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(info).toString()
        }.getOrDefault(packageName)
    }

    private fun Bundle.toJsonMap(): Map<String, Any?> {
        return keySet()
            .sorted()
            .associateWith { key -> get(key).toJsonValue() }
    }

    private fun Any?.toJsonValue(): Any? {
        return when (this) {
            null -> null
            is String -> this
            is CharSequence -> toString()
            is Number -> this
            is Boolean -> this
            is Bundle -> toJsonMap()
            is Array<*> -> map { it.toJsonValue() }
            is IntArray -> toList()
            is LongArray -> toList()
            is DoubleArray -> toList()
            is FloatArray -> map { it.toDouble() }
            is BooleanArray -> toList()
            is CharArray -> map { it.toString() }
            is Iterable<*> -> map { it.toJsonValue() }
            else -> toString()
        }
    }
}

object JsonFormatter {
    fun format(value: Any?): String = buildString {
        appendValue(value, level = 0)
    }

    private fun StringBuilder.appendValue(value: Any?, level: Int) {
        when (value) {
            null -> append("null")
            is String -> appendQuoted(value)
            is Number -> append(value)
            is Boolean -> append(value)
            is Map<*, *> -> appendObject(value, level)
            is Iterable<*> -> appendArray(value.toList(), level)
            else -> appendQuoted(value.toString())
        }
    }

    private fun StringBuilder.appendObject(value: Map<*, *>, level: Int) {
        if (value.isEmpty()) {
            append("{}")
            return
        }

        append("{\n")
        value.entries.forEachIndexed { index, entry ->
            appendIndent(level + 1)
            appendQuoted(entry.key.toString())
            append(": ")
            appendValue(entry.value, level + 1)
            if (index < value.size - 1) append(",")
            append("\n")
        }
        appendIndent(level)
        append("}")
    }

    private fun StringBuilder.appendArray(value: List<Any?>, level: Int) {
        if (value.isEmpty()) {
            append("[]")
            return
        }

        append("[\n")
        value.forEachIndexed { index, item ->
            appendIndent(level + 1)
            appendValue(item, level + 1)
            if (index < value.size - 1) append(",")
            append("\n")
        }
        appendIndent(level)
        append("]")
    }

    private fun StringBuilder.appendQuoted(value: String) {
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

    private fun StringBuilder.appendIndent(level: Int) {
        repeat(level) { append("  ") }
    }
}
