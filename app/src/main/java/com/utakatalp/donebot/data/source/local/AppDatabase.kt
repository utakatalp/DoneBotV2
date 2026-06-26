package com.utakatalp.donebot.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.utakatalp.donebot.data.model.entity.TaskEntity

@Database(entities = [TaskEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

}
