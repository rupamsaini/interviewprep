package com.rupamsaini.interviewprep.data.local

import com.rupamsaini.interviewprep.data.local.entity.QuestionEntity
import kotlinx.serialization.Serializable

@Serializable
data class QuestionDto(
    val question: String,
    val answer: String,
    val category: String,
    val difficulty: String,
    val source: String,
    val explanation: String? = null,
    val codeExample: String? = null
) {
    fun toEntity() = QuestionEntity(
        question = question,
        answer = answer,
        category = category,
        difficulty = difficulty,
        source = source,
        explanation = explanation,
        codeExample = codeExample
    )
}
