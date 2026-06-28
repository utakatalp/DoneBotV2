package com.utakatalp.donebot.data.repository

import com.utakatalp.donebot.common.handleLocal
import com.utakatalp.donebot.data.mapper.toDomain
import com.utakatalp.donebot.data.mapper.toEntity
import com.utakatalp.donebot.data.model.entity.SyncStatus
import com.utakatalp.donebot.data.model.entity.TaskEntity
import com.utakatalp.donebot.data.model.network.data.TaskData
import com.utakatalp.donebot.data.source.local.datasource.TaskLocalDataSource
import com.utakatalp.donebot.data.source.remote.datasource.TaskRemoteDataSource
import com.utakatalp.donebot.di.ApplicationScope
import com.utakatalp.donebot.di.SyncMutex
import com.utakatalp.donebot.domain.model.Task
import com.utakatalp.donebot.domain.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val localDataSource: TaskLocalDataSource,
    private val remoteDataSource: TaskRemoteDataSource,
    @ApplicationScope private val applicationScope: CoroutineScope,
    @SyncMutex private val syncMutex: Mutex,
) : TaskRepository {

    override fun getTasks(): Flow<List<Task>> =
        localDataSource.getAllTasks().map { list -> list.map { it.toDomain() } }

    override suspend fun getTaskById(id: Long): Task? =
        localDataSource.getTaskById(id)?.toDomain()

    override suspend fun addTask(task: Task): Result<Long> {
        val inserted = handleLocal {
            localDataSource.insertTask(task.toEntity(syncStatus = SyncStatus.PENDING_CREATE))
        }
        inserted.onSuccess { localId -> firePushCreate(localId, task) }
        return inserted
    }

    private fun firePushCreate(localId: Long, task: Task) {
        applicationScope.launch {
            remoteDataSource.createTask(task).onSuccess { remote ->
                val current = localDataSource.getTaskById(localId)
                if (current != null && current.syncStatus == SyncStatus.PENDING_CREATE) {
                    localDataSource.updateTask(
                        current.copy(remoteId = remote.id, syncStatus = SyncStatus.SYNCED),
                    )
                }
            }
        }
    }

    override suspend fun updateTask(task: Task): Result<Unit> {
        val existing = localDataSource.getTaskById(task.id)
            ?: return handleLocal { error("Task ${task.id} not found") }
        val orderIndex = existing.orderIndex
        return when (existing.syncStatus) {
            SyncStatus.PENDING_CREATE -> handleLocal {
                localDataSource.updateTask(
                    task.toEntity(syncStatus = SyncStatus.PENDING_CREATE, orderIndex = orderIndex),
                )
            }
            SyncStatus.PENDING_UPDATE -> handleLocal {
                localDataSource.updateTask(
                    task.copy(remoteId = existing.remoteId)
                        .toEntity(syncStatus = SyncStatus.PENDING_UPDATE, orderIndex = orderIndex),
                )
            }
            SyncStatus.PENDING_DELETE -> Result.success(Unit)
            SyncStatus.SYNCED -> updateRemoteFirst(task, existing)
        }
    }

    private suspend fun updateRemoteFirst(task: Task, existing: TaskEntity): Result<Unit> {
        val remoteId = existing.remoteId
            ?: return handleLocal {
                localDataSource.updateTask(
                    task.toEntity(syncStatus = SyncStatus.PENDING_UPDATE, orderIndex = existing.orderIndex),
                )
            }
        return remoteDataSource.updateTask(remoteId, task).fold(
            onSuccess = {
                handleLocal {
                    localDataSource.updateTask(
                        task.copy(remoteId = remoteId)
                            .toEntity(syncStatus = SyncStatus.SYNCED, orderIndex = existing.orderIndex),
                    )
                }
            },
            onFailure = {
                handleLocal {
                    localDataSource.updateTask(
                        task.copy(remoteId = remoteId)
                            .toEntity(syncStatus = SyncStatus.PENDING_UPDATE, orderIndex = existing.orderIndex),
                    )
                }
            },
        )
    }

    override suspend fun deleteTask(id: Long): Result<Unit> {
        val existing = localDataSource.getTaskById(id) ?: return Result.success(Unit)
        return when (existing.syncStatus) {
            SyncStatus.PENDING_CREATE -> handleLocal { localDataSource.deleteTask(id) }
            SyncStatus.PENDING_DELETE -> Result.success(Unit)
            SyncStatus.SYNCED, SyncStatus.PENDING_UPDATE -> deleteRemoteFirst(existing)
        }
    }

    private suspend fun deleteRemoteFirst(existing: TaskEntity): Result<Unit> {
        val remoteId = existing.remoteId
            ?: return handleLocal { localDataSource.deleteTask(existing.id) }
        return remoteDataSource.deleteTask(remoteId).fold(
            onSuccess = { handleLocal { localDataSource.deleteTask(existing.id) } },
            onFailure = {
                handleLocal {
                    localDataSource.updateTask(existing.copy(syncStatus = SyncStatus.PENDING_DELETE))
                }
            },
        )
    }

    override suspend fun clearAll() {
        localDataSource.deleteAll()
    }

    override suspend fun syncLocalTasksToServer(): Result<Unit> = syncMutex.withLock {
        val pending = localDataSource.findPending()
        var firstError: Throwable? = null
        for (entity in pending) {
            val result = when (entity.syncStatus) {
                SyncStatus.PENDING_CREATE -> syncCreated(entity)
                SyncStatus.PENDING_UPDATE -> syncUpdated(entity)
                SyncStatus.PENDING_DELETE -> syncDeleted(entity)
                SyncStatus.SYNCED -> Result.success(Unit)
            }
            result.onFailure { if (firstError == null) firstError = it }
        }
        firstError?.let { Result.failure(it) } ?: Result.success(Unit)
    }

    private suspend fun syncCreated(entity: TaskEntity): Result<Unit> =
        remoteDataSource.createTask(entity.toDomain()).mapCatching { remote ->
            localDataSource.updateTask(entity.copy(remoteId = remote.id, syncStatus = SyncStatus.SYNCED))
        }

    private suspend fun syncUpdated(entity: TaskEntity): Result<Unit> {
        val remoteId = entity.remoteId
            ?: return handleLocal {
                localDataSource.updateTask(entity.copy(syncStatus = SyncStatus.PENDING_CREATE))
            }
        return remoteDataSource.updateTask(remoteId, entity.toDomain()).mapCatching {
            localDataSource.updateTask(entity.copy(syncStatus = SyncStatus.SYNCED))
        }
    }

    private suspend fun syncDeleted(entity: TaskEntity): Result<Unit> {
        val remoteId = entity.remoteId
            ?: return handleLocal { localDataSource.deleteTask(entity.id) }
        return remoteDataSource.deleteTask(remoteId).mapCatching {
            localDataSource.deleteTask(entity.id)
        }
    }

    override suspend fun syncRemoteTasksWithLocal(): Result<Unit> = syncMutex.withLock {
        val fetchResult = remoteDataSource.getTasks()
        val list = fetchResult.getOrElse { return@withLock Result.failure(it) }
        val seen = mutableSetOf<Long>()
        for (remote in list.tasks) {
            seen.add(remote.id)
            val existing = localDataSource.getTaskByRemoteId(remote.id)
            when {
                existing == null -> localDataSource.insertTask(
                    remote.toEntity(syncStatus = SyncStatus.SYNCED),
                )
                existing.syncStatus == SyncStatus.SYNCED -> localDataSource.updateTask(
                    remote.toEntity(
                        localId = existing.id,
                        syncStatus = SyncStatus.SYNCED,
                        orderIndex = existing.orderIndex,
                    ),
                )
                else -> Unit
            }
        }
        val localSynced = localDataSource.findSyncedRemoteIds()
        val orphaned = localSynced.filter { it !in seen }
        if (orphaned.isNotEmpty()) {
            localDataSource.deleteSyncedByRemoteIds(orphaned)
        }
        Result.success(Unit)
    }
}
