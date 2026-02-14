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

    override suspend fun getRandomLocalQuestion(): Question? {
        val category = userPreferences.preferredCategory.first()
        val difficulty = userPreferences.preferredDifficulty.first()

        val hasCategory = category != "All"
        val hasDifficulty = difficulty != "All"
        // Map display-level difficulty to DB-level value (lowercase)
        val dbDifficulty = difficulty.lowercase().replace("-level", "")

        val entity = when {
            hasCategory && hasDifficulty -> dao.getRandomQuestionByCategoryAndDifficulty(category, dbDifficulty)
            hasCategory -> dao.getRandomQuestionByCategory(category)
            hasDifficulty -> dao.getRandomQuestionByDifficulty(dbDifficulty)
            else -> dao.getRandomQuestion()
        }
        return entity?.toDomain()
    }

    override suspend fun fetchNewQuestion(force: Boolean): Question? {
        val lastRequest = userPreferences.lastAiRequestTimestamp.first()
        val currentTime = System.currentTimeMillis()
        val oneHourMs = 3600 * 1000

        val canCallAi = true
        val randomChance = true

        if (canCallAi && randomChance) {
             try {
                 // Use user's preferred category/difficulty, or random if "All"
                 val prefCategory = userPreferences.preferredCategory.first()
                 val prefDifficulty = userPreferences.preferredDifficulty.first()

                 val categories = listOf("Kotlin", "Android", "Jetpack Compose", "Coroutines")
                 val difficulties = listOf("Junior", "Mid-Level", "Senior")
                 val category = if (prefCategory == "All") categories.random() else prefCategory
                 val difficulty = if (prefDifficulty == "All") difficulties.random() else prefDifficulty

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
