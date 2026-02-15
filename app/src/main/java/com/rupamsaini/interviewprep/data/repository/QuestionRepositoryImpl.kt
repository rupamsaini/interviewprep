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
import java.util.Calendar

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

        val canCallAi = true
        val randomChance = true

        if (canCallAi && randomChance) {
             try {
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

    override suspend fun deleteQuestions(scope: String): Int {
        return when (scope) {
            "All" -> dao.deleteAll()
            "Today" -> {
                val todayStart = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                dao.deleteCreatedAfter(todayStart)
            }
            // Category-based scopes prefixed with "cat:"
            else -> when {
                scope.startsWith("cat:") -> dao.deleteByCategory(scope.removePrefix("cat:"))
                scope.startsWith("diff:") -> {
                    val difficulty = scope.removePrefix("diff:").lowercase().replace("-level", "")
                    dao.deleteByDifficulty(difficulty)
                }
                else -> 0
            }
        }
    }
}
