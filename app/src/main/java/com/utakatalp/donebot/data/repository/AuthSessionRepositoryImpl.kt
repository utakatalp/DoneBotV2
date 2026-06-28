package com.utakatalp.donebot.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.utakatalp.donebot.domain.repository.AuthSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val TAG = "AuthFlow"

class AuthSessionRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : AuthSessionRepository {

    override suspend fun setAccessToken(token: String) {
        Log.d(TAG, "[AuthSession] setAccessToken ...${token.takeLast(8)}")
        dataStore.edit { it[ACCESS_TOKEN] = token }
    }

    override suspend fun getAccessToken(): String? =
        dataStore.data.map { it[ACCESS_TOKEN] }.first()?.ifBlank { null }

    override suspend fun setRefreshToken(token: String) {
        Log.d(TAG, "[AuthSession] setRefreshToken ...${token.takeLast(8)}")
        dataStore.edit { it[REFRESH_TOKEN] = token }
    }

    override suspend fun getRefreshToken(): String? =
        dataStore.data.map { it[REFRESH_TOKEN] }.first()?.ifBlank { null }

    override fun observeRefreshToken(): Flow<String?> =
        dataStore.data.map { it[REFRESH_TOKEN] }

    override suspend fun setExpiresAt(expiresIn: Long) {
        dataStore.edit { it[EXPIRES_AT] = expiresIn }
    }

    override suspend fun getExpiresAt(): Long? =
        dataStore.data.map { it[EXPIRES_AT] }.first()

    override suspend fun clear() {
        Log.d(TAG, "[AuthSession] clear() — wiping access + refresh + expiresAt", Throwable("clear caller"))
        dataStore.edit {
            it.remove(ACCESS_TOKEN)
            it.remove(REFRESH_TOKEN)
            it.remove(EXPIRES_AT)
        }
    }

    private companion object {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val EXPIRES_AT = longPreferencesKey("expires_at")
    }
}
