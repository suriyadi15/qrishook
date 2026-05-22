package io.github.suriyadi15.qrishook.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

class DebugNotificationRepository(
    private val debugNotificationDao: DebugNotificationDao,
) {
    fun recentLogs(): Flow<List<DebugNotificationEntity>> = debugNotificationDao.observeRecent()

    fun pagedLogs(query: String): Flow<PagingData<DebugNotificationEntity>> {
        return Pager(
            config = PagingConfig(pageSize = 30, initialLoadSize = 30),
            pagingSourceFactory = { debugNotificationDao.pagingSource(query.trim()) },
        ).flow
    }

    suspend fun insert(log: DebugNotificationEntity) {
        debugNotificationDao.insert(log)
    }

    suspend fun clear() {
        debugNotificationDao.clear()
    }
}
