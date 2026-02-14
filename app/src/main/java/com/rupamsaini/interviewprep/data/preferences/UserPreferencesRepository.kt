package com.rupamsaini.interviewprep.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val DAILY_NOTIFICATION_ENABLED = booleanPreferencesKey("daily_notification_enabled")
        val NOTIFICATION_TIME_HOUR = intPreferencesKey("notification_time_hour")
        val NOTIFICATION_TIME_MINUTE = intPreferencesKey("notification_time_minute")
        val WEEKEND_MODE_ENABLED = booleanPreferencesKey("weekend_mode_enabled")
        val LAST_AI_REQUEST_TIMESTAMP = longPreferencesKey("last_ai_request_timestamp")
    }

    val dailyNotificationEnabled: Flow<Boolean> = dataStore.data.map { it[DAILY_NOTIFICATION_ENABLED] ?: true }
    val notificationTimeHour: Flow<Int> = dataStore.data.map { it[NOTIFICATION_TIME_HOUR] ?: 9 }
    val notificationTimeMinute: Flow<Int> = dataStore.data.map { it[NOTIFICATION_TIME_MINUTE] ?: 0 }
    val weekendModeEnabled: Flow<Boolean> = dataStore.data.map { it[WEEKEND_MODE_ENABLED] ?: true }
    val lastAiRequestTimestamp: Flow<Long> = dataStore.data.map { it[LAST_AI_REQUEST_TIMESTAMP] ?: 0L }

    suspend fun setDailyNotificationEnabled(enabled: Boolean) {
        dataStore.edit { it[DAILY_NOTIFICATION_ENABLED] = enabled }
    }

    suspend fun setNotificationTime(hour: Int, minute: Int) {
        dataStore.edit {
            it[NOTIFICATION_TIME_HOUR] = hour
            it[NOTIFICATION_TIME_MINUTE] = minute
        }
    }

    suspend fun setWeekendModeEnabled(enabled: Boolean) {
        dataStore.edit { it[WEEKEND_MODE_ENABLED] = enabled }
    }

    suspend fun setLastAiRequestTimestamp(timestamp: Long) {
        dataStore.edit { it[LAST_AI_REQUEST_TIMESTAMP] = timestamp }
    }
}
