package com.utakatalp.donebot.data.model.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["date"]),
        Index(value = ["remote_id"], unique = true),
    ],
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Long = 0L,
    @ColumnInfo(name = "remote_id") val remoteId: Long? = null,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "description") val description: String? = null,
    @ColumnInfo(name = "date") val date: Long,
    @ColumnInfo(name = "time_start") val timeStart: Long,
    @ColumnInfo(name = "time_end") val timeEnd: Long,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean = false,
    @ColumnInfo(name = "sync_status", defaultValue = "PENDING_CREATE")
    val syncStatus: SyncStatus = SyncStatus.PENDING_CREATE,
    @ColumnInfo(name = "order_index", defaultValue = "0") val orderIndex: Int = 0,
)

enum class SyncStatus { PENDING_CREATE, PENDING_UPDATE, PENDING_DELETE, SYNCED }
