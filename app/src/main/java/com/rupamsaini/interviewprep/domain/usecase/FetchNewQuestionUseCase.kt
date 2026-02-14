package com.rupamsaini.interviewprep.domain.usecase

import com.rupamsaini.interviewprep.domain.model.Question
import com.rupamsaini.interviewprep.domain.repository.QuestionRepository
import javax.inject.Inject

class FetchNewQuestionUseCase @Inject constructor(
    private val repository: QuestionRepository
) {
    suspend operator fun invoke(force: Boolean = false): Question? {
        return repository.fetchNewQuestion(force)
    }
}
