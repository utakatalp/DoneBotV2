package com.utakatalp.donebot.data.source.local.converter

import androidx.room.TypeConverter
import com.utakatalp.donebot.data.model.entity.SyncStatus

class SyncStatusConverter {
    @TypeConverter
    fun fromSyncStatus(status: SyncStatus?): String? = status?.name

    @TypeConverter
    fun toSyncStatus(value: String?): SyncStatus? = value?.let { SyncStatus.valueOf(it) }
}
