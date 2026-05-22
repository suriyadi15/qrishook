package io.github.suriyadi15.qrishook.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debug_notification_logs")
data class DebugNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourcePackage: String,
    val sourceApp: String,
    val title: String,
    val text: String,
    val bigText: String,
    val postedAtMillis: Long,
    val capturedAtMillis: Long,
    val payloadJson: String,
)
