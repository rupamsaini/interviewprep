package com.rupamsaini.interviewprep.domain.usecase

import com.rupamsaini.interviewprep.domain.model.Question
import com.rupamsaini.interviewprep.domain.repository.QuestionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllQuestionsUseCase @Inject constructor(
    private val repository: QuestionRepository
) {
    operator fun invoke(): Flow<List<Question>> {
        return repository.getQuestions()
    }
}
