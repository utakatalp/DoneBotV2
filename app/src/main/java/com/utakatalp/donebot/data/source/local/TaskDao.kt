package com.utakatalp.donebot.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.utakatalp.donebot.data.model.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE sync_status != 'PENDING_DELETE' ORDER BY date ASC, time_start ASC, order_index ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE remote_id = :remoteId LIMIT 1")
    suspend fun getTaskByRemoteId(remoteId: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE sync_status != 'SYNCED'")
    suspend fun findPending(): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTask(id: Long)

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()

    @Query("SELECT remote_id FROM tasks WHERE remote_id IS NOT NULL AND sync_status = 'SYNCED'")
    suspend fun findSyncedRemoteIds(): List<Long>

    @Query("DELETE FROM tasks WHERE sync_status = 'SYNCED' AND remote_id IN (:remoteIds)")
    suspend fun deleteSyncedByRemoteIds(remoteIds: List<Long>)
}
