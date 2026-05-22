package io.github.suriyadi15.qrishook.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import io.github.suriyadi15.qrishook.domain.DeliveryStatus

@Database(
    entities = [EventEntity::class, DebugNotificationEntity::class],
    version = 4,
    exportSchema = false,
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
    abstract fun debugNotificationDao(): DebugNotificationDao
}

class AppTypeConverters {
    @TypeConverter
    fun deliveryStatusToString(status: DeliveryStatus): String = status.name

    @TypeConverter
    fun stringToDeliveryStatus(value: String): DeliveryStatus = DeliveryStatus.valueOf(value)
}
