package com.rupamsaini.interviewprep.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rupamsaini.interviewprep.domain.model.Question
import com.rupamsaini.interviewprep.domain.usecase.FetchNewQuestionUseCase
import com.rupamsaini.interviewprep.domain.usecase.GetAllQuestionsUseCase
import com.rupamsaini.interviewprep.domain.usecase.GetRandomLocalQuestionUseCase
import com.rupamsaini.interviewprep.domain.usecase.ImportQuestionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getAllQuestionsUseCase: GetAllQuestionsUseCase,
    private val fetchNewQuestionUseCase: FetchNewQuestionUseCase,
    private val getRandomLocalQuestionUseCase: GetRandomLocalQuestionUseCase,
    private val importQuestionsUseCase: ImportQuestionsUseCase
) : ViewModel() {

    companion object {
        val CATEGORIES = listOf("All", "Kotlin", "Android", "Jetpack Compose", "Coroutines")
        val DIFFICULTIES = listOf("All", "Junior", "Mid-Level", "Senior")
        val SOURCES = listOf("All", "Local", "AI", "Scraped")
    }

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _showSourcePicker = MutableStateFlow(false)
    val showSourcePicker: StateFlow<Boolean> = _showSourcePicker.asStateFlow()

    private val _fetchedQuestionId = MutableStateFlow<Long?>(null)
    val fetchedQuestionId: StateFlow<Long?> = _fetchedQuestionId.asStateFlow()

    // Filter state
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedDifficulty = MutableStateFlow("All")
    val selectedDifficulty: StateFlow<String> = _selectedDifficulty.asStateFlow()

    private val _selectedSource = MutableStateFlow("All")
    val selectedSource: StateFlow<String> = _selectedSource.asStateFlow()

    private val allQuestions: StateFlow<List<Question>> = getAllQuestionsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val questions: StateFlow<List<Question>> = combine(
        allQuestions,
        _selectedCategory,
        _selectedDifficulty,
        _selectedSource
    ) { questions, category, difficulty, source ->
        questions.filter { q ->
            val matchesCategory = category == "All" || q.category.equals(category, ignoreCase = true)
            val matchesDifficulty = difficulty == "All" || q.difficulty.equals(
                difficulty.lowercase().replace("-level", ""), ignoreCase = true
            )
            val matchesSource = source == "All" || q.source.equals(source, ignoreCase = true)
            matchesCategory && matchesDifficulty && matchesSource
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSelectedDifficulty(difficulty: String) {
        _selectedDifficulty.value = difficulty
    }

    fun setSelectedSource(source: String) {
        _selectedSource.value = source
    }

    fun onFetchQuestionClick() {
        _showSourcePicker.value = true
    }

    fun dismissSourcePicker() {
        _showSourcePicker.value = false
    }

    fun fetchFromDatabase() {
        _showSourcePicker.value = false
        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val question = getRandomLocalQuestionUseCase()
                _fetchedQuestionId.value = question?.id
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _isGenerating.value = false
        }
    }

    fun fetchFromAi() {
        _showSourcePicker.value = false
        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val question = fetchNewQuestionUseCase(force = true)
                _fetchedQuestionId.value = question?.id
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _isGenerating.value = false
        }
    }

    fun clearFetchedQuestion() {
        _fetchedQuestionId.value = null
    }

    fun importQuestions(url: String) {
        viewModelScope.launch {
            _isGenerating.value = true
            try {
                importQuestionsUseCase(url)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _isGenerating.value = false
        }
    }
}
