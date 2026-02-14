package com.rupamsaini.interviewprep.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rupamsaini.interviewprep.data.preferences.UserPreferencesRepository
import com.rupamsaini.interviewprep.domain.manager.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val dailyNotificationEnabled: Boolean = true,
    val notificationHour: Int = 9,
    val notificationMinute: Int = 0,
    val weekendModeEnabled: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesRepository.dailyNotificationEnabled,
        preferencesRepository.notificationTimeHour,
        preferencesRepository.notificationTimeMinute,
        preferencesRepository.weekendModeEnabled
    ) { enabled, hour, minute, weekendMode ->
        SettingsUiState(enabled, hour, minute, weekendMode)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun toggleDailyNotification(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDailyNotificationEnabled(enabled)
            if (enabled) {
                // Reschedule with current or default time
                val state = uiState.value
                notificationScheduler.scheduleDailyNotification(state.notificationHour, state.notificationMinute)
            } else {
                notificationScheduler.cancelDailyNotification()
            }
        }
    }

    fun setNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            preferencesRepository.setNotificationTime(hour, minute)
            if (uiState.value.dailyNotificationEnabled) {
                notificationScheduler.scheduleDailyNotification(hour, minute)
            }
        }
    }

    fun toggleWeekendMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setWeekendModeEnabled(enabled)
        }
    }
}
