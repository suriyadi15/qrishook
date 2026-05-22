package io.github.suriyadi15.qrishook.data

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DebugNotificationDao {
    @Insert
    suspend fun insert(log: DebugNotificationEntity)

    @Query("SELECT * FROM debug_notification_logs ORDER BY capturedAtMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int = 100): Flow<List<DebugNotificationEntity>>

    @Query(
        """
        SELECT * FROM debug_notification_logs
        WHERE :query = ''
            OR sourceApp LIKE '%' || :query || '%' COLLATE NOCASE
            OR sourcePackage LIKE '%' || :query || '%' COLLATE NOCASE
            OR title LIKE '%' || :query || '%' COLLATE NOCASE
            OR text LIKE '%' || :query || '%' COLLATE NOCASE
            OR bigText LIKE '%' || :query || '%' COLLATE NOCASE
            OR payloadJson LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY capturedAtMillis DESC
        """,
    )
    fun pagingSource(query: String): PagingSource<Int, DebugNotificationEntity>

    @Query("DELETE FROM debug_notification_logs")
    suspend fun clear()
}
