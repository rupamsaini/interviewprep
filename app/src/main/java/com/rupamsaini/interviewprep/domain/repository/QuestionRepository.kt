package com.rupamsaini.interviewprep.domain.repository

import com.rupamsaini.interviewprep.domain.model.Question
import kotlinx.coroutines.flow.Flow

interface QuestionRepository {
    
    fun getQuestions(): Flow<List<Question>>
    
    suspend fun getQuestionById(id: Long): Question?
    
    suspend fun insertQuestion(question: Question)
    
    suspend fun updateQuestion(question: Question)
    
    suspend fun deleteQuestion(question: Question)
    
    suspend fun getQuestionsByCategory(category: String): List<Question>
    
    suspend fun fetchNewQuestion(force: Boolean = false): Question?
    
    suspend fun processReview(question: Question, quality: Int)
    
    suspend fun importQuestionsFromUrl(url: String): Int

    suspend fun getRandomLocalQuestion(): Question?

    suspend fun deleteQuestions(scope: String): Int
}
