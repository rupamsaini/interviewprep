package com.rupamsaini.interviewprep.data.remote

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.rupamsaini.interviewprep.BuildConfig
import com.rupamsaini.interviewprep.domain.model.Question
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiDataSource @Inject constructor() {

    private val generativeModel = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel("gemini-2.5-flash")

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun generateQuestion(topic: String, difficulty: String): Question? {
        // If API key is missing (e.g. CI/CD or initial setup), return null gracefully
        if (BuildConfig.GEMINI_API_KEY.isEmpty()) return null

        val prompt = """
            Generate a single mobile development interview question for a '$difficulty' level candidate about '$topic'.
            The question should be relevant to mobile/Android development.
            Return strictly valid JSON with no markdown formatting.
            Structure:
            {
              "question": "The question text",
              "answer": "Concise answer",
              "explanation": "Brief explanation",
              "codeExample": "Optional code snippet or null"
            }
        """.trimIndent()

        return try {
            val response = generativeModel.generateContent(prompt)
            val responseText = response.text?.replace("```json", "")?.replace("```", "")?.trim()
            
            if (responseText != null) {
                val dto = json.decodeFromString<GeminiQuestionDto>(responseText)
                dto.toDomain(topic, difficulty)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

@kotlinx.serialization.Serializable
data class GeminiQuestionDto(
    val question: String,
    val answer: String,
    val explanation: String? = null,
    val codeExample: String? = null
) {
    fun toDomain(category: String, difficulty: String) = Question(
        question = question,
        answer = answer,
        category = category,
        difficulty = difficulty,
        source = "ai",
        explanation = explanation,
        codeExample = codeExample
    )
}
