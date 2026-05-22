package io.github.suriyadi15.qrishook.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import io.github.suriyadi15.qrishook.domain.DeliveryStatus
import io.github.suriyadi15.qrishook.domain.QrisPaymentEvent
import io.github.suriyadi15.qrishook.webhook.WebhookResult
import kotlinx.coroutines.flow.Flow

class EventRepository(
    private val eventDao: EventDao,
) : EventDeliveryStore {
    fun recentEvents(): Flow<List<EventEntity>> = eventDao.observeRecent()

    fun pagedEvents(query: String): Flow<PagingData<EventEntity>> {
        return Pager(
            config = PagingConfig(pageSize = 30, initialLoadSize = 30),
            pagingSourceFactory = { eventDao.pagingSource(query.trim()) },
        ).flow
    }

    suspend fun enqueue(event: QrisPaymentEvent): EventEntity {
        val entity = EventEntity.fromEvent(event)
        eventDao.insert(entity)
        return entity
    }

    suspend fun pendingBatch(): List<EventEntity> = eventDao.pending()

    override suspend fun markSending(eventId: String) {
        eventDao.updateStatus(
            eventId = eventId,
            status = DeliveryStatus.Sending,
            lastError = "",
        )
    }

    override suspend fun markSent(eventId: String, result: WebhookResult.Success) {
        eventDao.updateDeliveryResult(
            eventId = eventId,
            status = DeliveryStatus.Sent,
            lastError = "",
            lastResponseCode = result.code,
            lastResponseMessage = result.message,
            lastResponseBody = result.body,
        )
    }

    override suspend fun markFailed(eventId: String, result: WebhookResult.Failure) {
        eventDao.updateDeliveryResult(
            eventId = eventId,
            status = DeliveryStatus.Failed,
            lastError = result.reason.take(500),
            lastResponseCode = result.code,
            lastResponseMessage = result.message,
            lastResponseBody = result.body,
        )
    }
}

interface EventDeliveryStore {
    suspend fun markSending(eventId: String)
    suspend fun markSent(eventId: String, result: WebhookResult.Success)
    suspend fun markFailed(eventId: String, result: WebhookResult.Failure)
}
