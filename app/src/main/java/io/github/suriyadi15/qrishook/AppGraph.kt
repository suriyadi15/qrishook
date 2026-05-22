package io.github.suriyadi15.qrishook

import android.content.Context
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import io.github.suriyadi15.qrishook.data.AppDatabase
import io.github.suriyadi15.qrishook.data.DebugNotificationRepository
import io.github.suriyadi15.qrishook.data.EventRepository
import io.github.suriyadi15.qrishook.data.SettingsRepository
import io.github.suriyadi15.qrishook.merchant.QrisNotificationMatcher
import io.github.suriyadi15.qrishook.update.GitHubUpdateChecker
import io.github.suriyadi15.qrishook.webhook.WebhookClient
import io.github.suriyadi15.qrishook.webhook.WebhookDeliveryRunner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object AppGraph {
    @Volatile private var instance: Container? = null

    fun get(context: Context): Container {
        return instance ?: synchronized(this) {
            instance ?: Container(context.applicationContext).also { instance = it }
        }
    }

    class Container(context: Context) {
        val database: AppDatabase = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "qris_hook.db",
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4).build()

        val settingsRepository = SettingsRepository(context)
        val eventRepository = EventRepository(database.eventDao())
        val debugNotificationRepository = DebugNotificationRepository(database.debugNotificationDao())
        val matcher = QrisNotificationMatcher()
        val webhookClient = WebhookClient()
        val webhookDeliveryRunner = WebhookDeliveryRunner(eventRepository, webhookClient)
        val updateChecker = GitHubUpdateChecker()
        val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS debug_notification_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    sourcePackage TEXT NOT NULL,
                    sourceApp TEXT NOT NULL,
                    title TEXT NOT NULL,
                    text TEXT NOT NULL,
                    bigText TEXT NOT NULL,
                    postedAtMillis INTEGER NOT NULL,
                    capturedAtMillis INTEGER NOT NULL,
                    payloadJson TEXT NOT NULL
                )
                """.trimIndent(),
            )
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE qris_events ADD COLUMN senderName TEXT")
            db.execSQL("ALTER TABLE qris_events ADD COLUMN paymentSource TEXT")
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE qris_events ADD COLUMN lastResponseCode INTEGER")
            db.execSQL("ALTER TABLE qris_events ADD COLUMN lastResponseMessage TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE qris_events ADD COLUMN lastResponseBody TEXT NOT NULL DEFAULT ''")
            db.execSQL("ALTER TABLE qris_events ADD COLUMN lastWebhookAttemptAtMillis INTEGER")
        }
    }
}
