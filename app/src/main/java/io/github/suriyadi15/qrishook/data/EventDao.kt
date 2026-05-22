package io.github.suriyadi15.qrishook.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.paging.PagingSource
import io.github.suriyadi15.qrishook.domain.DeliveryStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(event: EventEntity)

    @Query("SELECT * FROM qris_events ORDER BY createdAtMillis DESC LIMIT :limit")
    fun observeRecent(limit: Int = 50): Flow<List<EventEntity>>

    @Query(
        """
        SELECT * FROM qris_events
        WHERE :query = ''
            OR sourceApp LIKE '%' || :query || '%' COLLATE NOCASE
            OR sourcePackage LIKE '%' || :query || '%' COLLATE NOCASE
            OR merchantId LIKE '%' || :query || '%' COLLATE NOCASE
            OR title LIKE '%' || :query || '%' COLLATE NOCASE
            OR text LIKE '%' || :query || '%' COLLATE NOCASE
            OR bigText LIKE '%' || :query || '%' COLLATE NOCASE
            OR senderName LIKE '%' || :query || '%' COLLATE NOCASE
            OR paymentSource LIKE '%' || :query || '%' COLLATE NOCASE
            OR status LIKE '%' || :query || '%' COLLATE NOCASE
            OR CAST(amount AS TEXT) LIKE '%' || :query || '%'
        ORDER BY createdAtMillis DESC
        """,
    )
    fun pagingSource(query: String): PagingSource<Int, EventEntity>

    @Query("SELECT * FROM qris_events WHERE status != 'Sent' ORDER BY createdAtMillis ASC LIMIT :limit")
    suspend fun pending(limit: Int = 10): List<EventEntity>

    @Query(
        """
        UPDATE qris_events
        SET status = :status,
            lastError = :lastError,
            updatedAtMillis = :updatedAtMillis
        WHERE eventId = :eventId
        """,
    )
    suspend fun updateStatus(
        eventId: String,
        status: DeliveryStatus,
        lastError: String,
        updatedAtMillis: Long = System.currentTimeMillis(),
    )

    @Query(
        """
        UPDATE qris_events
        SET status = :status,
            attempts = attempts + 1,
            lastError = :lastError,
            lastResponseCode = :lastResponseCode,
            lastResponseMessage = :lastResponseMessage,
            lastResponseBody = :lastResponseBody,
            lastWebhookAttemptAtMillis = :lastWebhookAttemptAtMillis,
            updatedAtMillis = :updatedAtMillis
        WHERE eventId = :eventId
        """,
    )
    suspend fun updateDeliveryResult(
        eventId: String,
        status: DeliveryStatus,
        lastError: String,
        lastResponseCode: Int?,
        lastResponseMessage: String,
        lastResponseBody: String,
        lastWebhookAttemptAtMillis: Long = System.currentTimeMillis(),
        updatedAtMillis: Long = System.currentTimeMillis(),
    )
}
