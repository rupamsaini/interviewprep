package com.rupamsaini.interviewprep.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rupamsaini.interviewprep.domain.model.Question
import com.rupamsaini.interviewprep.domain.usecase.GetAllQuestionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getAllQuestionsUseCase: GetAllQuestionsUseCase,
    private val fetchNewQuestionUseCase: com.rupamsaini.interviewprep.domain.usecase.FetchNewQuestionUseCase,
    private val importQuestionsUseCase: com.rupamsaini.interviewprep.domain.usecase.ImportQuestionsUseCase
) : ViewModel() {

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    val questions: StateFlow<List<Question>> = getAllQuestionsUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    fun onGenerateQuestionClick() {
        viewModelScope.launch {
            _isGenerating.value = true
            fetchNewQuestionUseCase(force = true)
            _isGenerating.value = false
        }
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
