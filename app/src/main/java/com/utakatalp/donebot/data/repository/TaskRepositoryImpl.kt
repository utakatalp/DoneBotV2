package com.utakatalp.donebot.data.repository

import com.utakatalp.donebot.common.DomainException.Unauthorized
import com.utakatalp.donebot.common.handleLocal
import com.utakatalp.donebot.data.mapper.toDomain
import com.utakatalp.donebot.data.mapper.toEntity
import com.utakatalp.donebot.data.model.entity.SyncStatus
import com.utakatalp.donebot.data.model.entity.TaskEntity
import com.utakatalp.donebot.data.source.local.datasource.TaskLocalDataSource
import com.utakatalp.donebot.data.source.remote.datasource.TaskRemoteDataSource
import com.utakatalp.donebot.di.ApplicationScope
import com.utakatalp.donebot.di.SyncMutex
import com.utakatalp.donebot.domain.model.Task
import com.utakatalp.donebot.domain.repository.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val localDataSource: TaskLocalDataSource,
    private val remoteDataSource: TaskRemoteDataSource,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
    @param:SyncMutex private val syncMutex: Mutex,
) : TaskRepository {

    override fun getTasks(): Flow<List<Task>> =
        localDataSource.getAllTasks().map { list -> list.map { it.toDomain() } }

    override suspend fun getTaskById(id: Long): Task? = withContext(Dispatchers.IO) {
        localDataSource.getTaskById(id)?.toDomain()
    }

    override suspend fun addTask(task: Task): Result<Long> = withContext(Dispatchers.IO) {
        val inserted = handleLocal {
            localDataSource.insertTask(task.toEntity(syncStatus = SyncStatus.PENDING_CREATE))
        }
        inserted.onSuccess { localId -> firePushCreate(localId, task) }
        inserted
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

    override suspend fun updateTask(task: Task): Result<Unit> = withContext(Dispatchers.IO) {
        val existing = localDataSource.getTaskById(task.id)
            ?: return@withContext handleLocal { error("Task ${task.id} not found") }
        val orderIndex = existing.orderIndex
        when (existing.syncStatus) {
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

    override suspend fun deleteTask(id: Long): Result<Unit> = withContext(Dispatchers.IO) {
        val existing = localDataSource.getTaskById(id) ?: return@withContext Result.success(Unit)
        when (existing.syncStatus) {
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

    override suspend fun clearAll() = withContext(Dispatchers.IO) {
        localDataSource.deleteAll()
    }

    override suspend fun syncLocalTasksToServer(): Result<Unit> = withContext(Dispatchers.IO) {
        syncMutex.withLock { doSyncLocalToServer() }
    }

    private suspend fun doSyncLocalToServer(): Result<Unit> {
        val pending = localDataSource.findPending()
        var firstError: Throwable? = null
        for (entity in pending) {
            val result = when (entity.syncStatus) {
                SyncStatus.PENDING_CREATE -> syncCreated(entity)
                SyncStatus.PENDING_UPDATE -> syncUpdated(entity)
                SyncStatus.PENDING_DELETE -> syncDeleted(entity)
                SyncStatus.SYNCED -> Result.success(Unit)
            }
            result.onFailure {
                if (firstError == null) firstError = it
                // Bail on the first Unauthorized: every remaining row would hit the same
                // 401 (same broken auth state). The authenticator already tried to refresh.
                if (it is Unauthorized) {
                    return Result.failure(it)
                }
            }
        }
        return firstError?.let { Result.failure(it) } ?: Result.success(Unit)
    }

    private suspend fun syncCreated(entity: TaskEntity): Result<Unit> {
        return remoteDataSource.createTask(entity.toDomain()).mapCatching { remote ->
            localDataSource.updateTask(entity.copy(remoteId = remote.id, syncStatus = SyncStatus.SYNCED))
        }.onFailure {
        }
    }

    private suspend fun syncUpdated(entity: TaskEntity): Result<Unit> {
        val remoteId = entity.remoteId
        // 404 means the server-side row is gone (e.g., another device deleted it
        // while this device was editing offline). Edit-beats-delete: preserve the
        // user's edits by clearing remoteId and demoting to PENDING_CREATE so the
        // next sync POSTs a fresh copy. Return success so the per-row loop in
        // syncLocalTasksToServer continues to the next entity.
        when (remoteId) {
            null -> {
                return handleLocal {
                    localDataSource.updateTask(entity.copy(syncStatus = SyncStatus.PENDING_CREATE))
                }
            }

            else -> return remoteDataSource.updateTask(remoteId, entity.toDomain()).fold(
                onSuccess = {
                    handleLocal {
                        localDataSource.updateTask(entity.copy(syncStatus = SyncStatus.SYNCED))
                    }
                },
                onFailure = { error ->
                    // 404 means the server-side row is gone (e.g., another device deleted it
                    // while this device was editing offline). Edit-beats-delete: preserve the
                    // user's edits by clearing remoteId and demoting to PENDING_CREATE so the
                    // next sync POSTs a fresh copy. Return success so the per-row loop in
                    // syncLocalTasksToServer continues to the next entity.
                    if (error is com.utakatalp.donebot.common.DomainException.NotFound) {
                        handleLocal {
                            localDataSource.updateTask(
                                entity.copy(remoteId = null, syncStatus = SyncStatus.PENDING_CREATE),
                            )
                        }
                    } else {
                        Result.failure(error)
                    }
                },
            )
        }
    }

    private suspend fun syncDeleted(entity: TaskEntity): Result<Unit> {
        val remoteId = entity.remoteId
        if (remoteId == null) {
            return handleLocal { localDataSource.deleteTask(entity.id) }
        }
        return remoteDataSource.deleteTask(remoteId).fold(
            onSuccess = {
                handleLocal { localDataSource.deleteTask(entity.id) }
            },
            onFailure = { error ->
                // 404 from the server means the row is already gone there — our local PENDING_DELETE
                // marker has nothing left to push. Prune the local row so it stops retrying forever.
                if (error is com.utakatalp.donebot.common.DomainException.NotFound) {
                    handleLocal { localDataSource.deleteTask(entity.id) }
                } else {
                    Result.failure(error)
                }
            },
        )
    }

    override suspend fun syncRemoteTasksWithLocal(): Result<Unit> = withContext(Dispatchers.IO) {
        syncMutex.withLock { doSyncRemoteWithLocal() }
    }

    private suspend fun doSyncRemoteWithLocal(): Result<Unit> {
        val fetchResult = remoteDataSource.getTasks()
        val list = fetchResult.getOrElse {
            return Result.failure(it)
        }
        val seen = mutableSetOf<Long>()
        var inserts = 0
        var overwrites = 0
        var skipped = 0
        for (remote in list.tasks) {
            seen.add(remote.id)
            val existing = localDataSource.getTaskByRemoteId(remote.id)
            when {
                existing == null -> {
                    localDataSource.insertTask(remote.toEntity(syncStatus = SyncStatus.SYNCED))
                    inserts++
                }
                existing.syncStatus == SyncStatus.SYNCED -> {
                    localDataSource.updateTask(
                        remote.toEntity(
                            localId = existing.id,
                            syncStatus = SyncStatus.SYNCED,
                            orderIndex = existing.orderIndex,
                        ),
                    )
                    overwrites++
                }
                else -> {
                    // PENDING_* row — local wins until pushed
                    skipped++
                }
            }
        }
        val localSynced = localDataSource.findSyncedRemoteIds()
        val orphaned = localSynced.filter { it !in seen }
        if (orphaned.isNotEmpty()) {
            localDataSource.deleteSyncedByRemoteIds(orphaned)
        }
        return Result.success(Unit)
    }
}
