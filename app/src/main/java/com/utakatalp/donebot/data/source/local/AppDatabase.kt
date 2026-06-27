package com.utakatalp.donebot.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.utakatalp.donebot.data.model.entity.TaskEntity
import com.utakatalp.donebot.data.source.local.converter.SyncStatusConverter

@Database(entities = [TaskEntity::class], version = 2, exportSchema = false)
@TypeConverters(SyncStatusConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

}
