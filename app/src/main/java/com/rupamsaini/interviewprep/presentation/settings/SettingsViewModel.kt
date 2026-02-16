package com.rupamsaini.interviewprep.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rupamsaini.interviewprep.data.preferences.UserPreferencesRepository
import com.rupamsaini.interviewprep.domain.manager.NotificationScheduler
import com.rupamsaini.interviewprep.domain.repository.QuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val dailyNotificationEnabled: Boolean = true,
    val notificationHour: Int = 9,
    val notificationMinute: Int = 0,
    val weekendModeEnabled: Boolean = true,
    val preferredDifficulty: String = "All",
    val preferredCategory: String = "All",
    val autoDeleteScope: String = "All",
    val autoDeleteScheduled: Boolean = false,
    val autoDeleteHour: Int = 0,
    val autoDeleteMinute: Int = 0,
    val selectedTopics: Set<String> = ALL_TOPICS
) {
    companion object {
        val ALL_TOPICS = setOf(
            "Kotlin", "Android", "Jetpack Compose", "Coroutines",
            "System Design", "Design Patterns", "Security",
            "Performance Optimization", "Testing & QA", "Networking & APIs"
        )
    }
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val notificationScheduler: NotificationScheduler,
    private val questionRepository: QuestionRepository
) : ViewModel() {

    companion object {
        val DIFFICULTIES = listOf("All", "Junior", "Mid-Level", "Senior")
        val CATEGORIES = listOf(
            "All", "Kotlin", "Android", "Jetpack Compose", "Coroutines",
            "System Design", "Design Patterns", "Security",
            "Performance Optimization", "Testing & QA", "Networking & APIs"
        )
        val DELETE_SCOPES = listOf(
            "All" to "All Questions",
            "Today" to "Today's Questions",
            "cat:Kotlin" to "Category: Kotlin",
            "cat:Android" to "Category: Android",
            "cat:Jetpack Compose" to "Category: Jetpack Compose",
            "cat:Coroutines" to "Category: Coroutines",
            "cat:System Design" to "Category: System Design",
            "cat:Design Patterns" to "Category: Design Patterns",
            "cat:Security" to "Category: Security",
            "cat:Performance Optimization" to "Category: Performance Optimization",
            "cat:Testing & QA" to "Category: Testing & QA",
            "cat:Networking & APIs" to "Category: Networking & APIs",
            "diff:Junior" to "Difficulty: Junior",
            "diff:Mid-Level" to "Difficulty: Mid-Level",
            "diff:Senior" to "Difficulty: Senior"
        )
    }

    private val _deleteResult = MutableStateFlow<String?>(null)
    val deleteResult: StateFlow<String?> = _deleteResult.asStateFlow()

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(
            preferencesRepository.dailyNotificationEnabled,
            preferencesRepository.notificationTimeHour,
            preferencesRepository.notificationTimeMinute,
            preferencesRepository.weekendModeEnabled,
            preferencesRepository.preferredDifficulty,
        ) { values -> values },
        combine(
            preferencesRepository.preferredCategory,
            preferencesRepository.autoDeleteScope,
            preferencesRepository.autoDeleteScheduled,
            preferencesRepository.autoDeleteHour,
            preferencesRepository.autoDeleteMinute
        ) { values -> values },
        preferencesRepository.selectedTopics
    ) { group1, group2, topics ->
        SettingsUiState(
            dailyNotificationEnabled = group1[0] as Boolean,
            notificationHour = group1[1] as Int,
            notificationMinute = group1[2] as Int,
            weekendModeEnabled = group1[3] as Boolean,
            preferredDifficulty = group1[4] as String,
            preferredCategory = group2[0] as String,
            autoDeleteScope = group2[1] as String,
            autoDeleteScheduled = group2[2] as Boolean,
            autoDeleteHour = group2[3] as Int,
            autoDeleteMinute = group2[4] as Int,
            selectedTopics = topics
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun toggleDailyNotification(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDailyNotificationEnabled(enabled)
            if (enabled) {
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

    fun setPreferredDifficulty(difficulty: String) {
        viewModelScope.launch {
            preferencesRepository.setPreferredDifficulty(difficulty)
        }
    }

    fun setPreferredCategory(category: String) {
        viewModelScope.launch {
            preferencesRepository.setPreferredCategory(category)
        }
    }

    fun setAutoDeleteScope(scope: String) {
        viewModelScope.launch {
            preferencesRepository.setAutoDeleteScope(scope)
        }
    }

    fun toggleScheduledDelete(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setAutoDeleteScheduled(enabled)
            if (enabled) {
                val state = uiState.value
                notificationScheduler.scheduleDailyDeletion(state.autoDeleteHour, state.autoDeleteMinute)
            } else {
                notificationScheduler.cancelDailyDeletion()
            }
        }
    }

    fun setAutoDeleteTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            preferencesRepository.setAutoDeleteTime(hour, minute)
            if (uiState.value.autoDeleteScheduled) {
                notificationScheduler.scheduleDailyDeletion(hour, minute)
            }
        }
    }

    fun deleteNow() {
        viewModelScope.launch {
            try {
                val scope = uiState.value.autoDeleteScope
                val count = questionRepository.deleteQuestions(scope)
                val label = DELETE_SCOPES.find { it.first == scope }?.second ?: scope
                _deleteResult.value = "Deleted $count questions ($label)"
            } catch (e: Exception) {
                _deleteResult.value = "Failed to delete: ${e.message}"
            }
        }
    }

    fun clearDeleteResult() {
        _deleteResult.value = null
    }

    fun toggleTopic(topic: String) {
        viewModelScope.launch {
            val current = uiState.value.selectedTopics
            val updated = if (current.contains(topic)) current - topic else current + topic
            if (updated.isNotEmpty()) preferencesRepository.setSelectedTopics(updated)
        }
    }

    fun selectAllTopics() {
        viewModelScope.launch {
            preferencesRepository.setSelectedTopics(SettingsUiState.ALL_TOPICS)
        }
    }

    fun deselectAllTopics() {
        // Keep at least one topic selected to avoid empty state
    }
}
