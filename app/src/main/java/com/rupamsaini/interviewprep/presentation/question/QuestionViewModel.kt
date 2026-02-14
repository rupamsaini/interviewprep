package com.rupamsaini.interviewprep.presentation.question

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rupamsaini.interviewprep.domain.model.Question
import com.rupamsaini.interviewprep.domain.repository.QuestionRepository
import com.rupamsaini.interviewprep.domain.usecase.GetQuestionByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestionViewModel @Inject constructor(
    private val getQuestionByIdUseCase: GetQuestionByIdUseCase,
    private val repository: QuestionRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _question = MutableStateFlow<Question?>(null)
    val question: StateFlow<Question?> = _question.asStateFlow()

    // UI State to toggle answer visibility
    private val _isAnswerVisible = MutableStateFlow(false)
    val isAnswerVisible: StateFlow<Boolean> = _isAnswerVisible.asStateFlow()

    init {
        savedStateHandle.get<Long>("questionId")?.let { id ->
            loadQuestion(id)
        }
    }

    private fun loadQuestion(id: Long) {
        viewModelScope.launch {
            _question.value = getQuestionByIdUseCase(id)
        }
    }

    fun toggleAnswerVisibility() {
        _isAnswerVisible.value = !_isAnswerVisible.value
    }

    fun processReview(quality: Int) {
        viewModelScope.launch {
            val currentQuestion = _question.value
            if (currentQuestion != null) {
                repository.processReview(currentQuestion, quality)
                // Optionally navigate back or advance to next question
            }
        }
    }
}
