package com.rupamsaini.interviewprep.domain.usecase

import com.rupamsaini.interviewprep.domain.repository.QuestionRepository
import javax.inject.Inject

class ImportQuestionsUseCase @Inject constructor(
    private val repository: QuestionRepository
) {
    suspend operator fun invoke(url: String): Int {
        return repository.importQuestionsFromUrl(url)
    }
}
