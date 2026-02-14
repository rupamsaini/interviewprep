package com.rupamsaini.interviewprep.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rupamsaini.interviewprep.domain.model.Question

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val question: String,
    val answer: String,
    val category: String,
    val difficulty: String,
    val source: String,
    val explanation: String? = null,
    val codeExample: String? = null,
    val lastShown: Long = 0,
    val userRating: Int = 0,

    // Spaced Repetition
    val repetition: Int = 0,
    val easinessFactor: Float = 2.5f,
    val interval: Int = 0,
    val nextReviewDate: Long = 0
) {
    fun toDomain() = Question(
        id = id,
        question = question,
        answer = answer,
        category = category,
        difficulty = difficulty,
        source = source,
        explanation = explanation,
        codeExample = codeExample,
        lastShown = lastShown,
        userRating = userRating,
        repetition = repetition,
        easinessFactor = easinessFactor,
        interval = interval,
        nextReviewDate = nextReviewDate
    )
}
    
fun Question.toEntity() = QuestionEntity(
    id = id,
    question = question,
    answer = answer,
    category = category,
    difficulty = difficulty,
    source = source,
    explanation = explanation,
    codeExample = codeExample,
    lastShown = lastShown,
    userRating = userRating,
    repetition = repetition,
    easinessFactor = easinessFactor,
    interval = interval,
    nextReviewDate = nextReviewDate
)
