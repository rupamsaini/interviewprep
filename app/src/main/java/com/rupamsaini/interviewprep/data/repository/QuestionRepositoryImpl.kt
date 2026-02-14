package com.rupamsaini.interviewprep.data.repository

import com.rupamsaini.interviewprep.data.local.QuestionDao
import com.rupamsaini.interviewprep.data.local.entity.toEntity
import com.rupamsaini.interviewprep.domain.model.Question
import com.rupamsaini.interviewprep.domain.repository.QuestionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

import com.rupamsaini.interviewprep.data.preferences.UserPreferencesRepository
import com.rupamsaini.interviewprep.data.remote.GeminiDataSource
import com.rupamsaini.interviewprep.data.remote.WebScraperDataSource
import com.rupamsaini.interviewprep.domain.util.SpacedRepetitionCalculator
import kotlinx.coroutines.flow.first
import kotlin.random.Random

class QuestionRepositoryImpl @Inject constructor(
    private val dao: QuestionDao,
    private val geminiDataSource: GeminiDataSource,
    private val userPreferences: UserPreferencesRepository,
    private val webScraperDataSource: WebScraperDataSource,
    private val spacedRepetitionCalculator: SpacedRepetitionCalculator
) : QuestionRepository {

    override fun getQuestions(): Flow<List<Question>> {
        return dao.getAllQuestions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getQuestionById(id: Long): Question? {
        return dao.getQuestionById(id)?.toDomain()
    }

    override suspend fun insertQuestion(question: Question) {
        dao.insert(question.toEntity())
    }

    override suspend fun updateQuestion(question: Question) {
        dao.update(question.toEntity())
    }

    override suspend fun deleteQuestion(question: Question) {
        dao.delete(question.toEntity())
    }

    override suspend fun getQuestionsByCategory(category: String): List<Question> {
        return dao.getQuestionsByCategory(category).map { it.toDomain() }
    }
    
    override suspend fun processReview(question: Question, quality: Int) {
        val updatedQuestion = spacedRepetitionCalculator.calculateNextReview(question, quality)
        dao.update(updatedQuestion.toEntity())
    }

    override suspend fun importQuestionsFromUrl(url: String): Int {
        val questions = webScraperDataSource.scrapeQuestions(url)
        dao.insertAll(questions.map { it.toEntity() })
        return questions.size
    }

    override suspend fun fetchNewQuestion(force: Boolean): Question? {
        // Logic:
        // 1. Check Rate Limit (1 hour)
        // 2. 5% Probability check
        // 3. Call AI
        // 4. Fallback to local random if AI skipped or failed

        val lastRequest = userPreferences.lastAiRequestTimestamp.first()
        val currentTime = System.currentTimeMillis()
        val oneHourMs = 3600 * 1000

        val canCallAi = true
//        val canCallAi = force || (currentTime - lastRequest) > oneHourMs
//        val randomChance = force || Random.nextFloat() < 0.05f // 5% chance
        val randomChance = true // 5% chance

        // Let's try to fetch from AI if allowed.
        if (canCallAi && randomChance) {
             try {
                 // Random category/difficulty for variety
                 val categories = listOf("Kotlin", "Android", "Jetpack Compose", "Coroutines")
                 val difficulties = listOf("Junior", "Mid-Level", "Senior")
                 val category = categories.random()
                 val difficulty = difficulties.random()

                 val aiQuestion = geminiDataSource.generateQuestion(category, difficulty)
                 if (aiQuestion != null) {
                     userPreferences.setLastAiRequestTimestamp(currentTime)
                     insertQuestion(aiQuestion)
                     return aiQuestion
                 }
             } catch (e: Exception) {
                 e.printStackTrace()
             }
        }
        
        return null
    }
}
