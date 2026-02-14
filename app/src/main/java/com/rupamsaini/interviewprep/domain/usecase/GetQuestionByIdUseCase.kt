package com.rupamsaini.interviewprep.domain.usecase

import com.rupamsaini.interviewprep.domain.model.Question
import com.rupamsaini.interviewprep.domain.repository.QuestionRepository
import javax.inject.Inject

class GetQuestionByIdUseCase @Inject constructor(
    private val repository: QuestionRepository
) {
    suspend operator fun invoke(id: Long): Question? {
        return repository.getQuestionById(id)
    }
}
