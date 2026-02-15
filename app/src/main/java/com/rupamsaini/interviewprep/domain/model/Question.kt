package com.rupamsaini.interviewprep.domain.model

data class Question(
    val id: Long = 0,
    val question: String,
    val answer: String,
    val category: String,
    val difficulty: String,
    val source: String,
    val explanation: String? = null,
    val codeExample: String? = null,
    val lastShown: Long = 0,
    val userRating: Int = 0,
    val createdAt: Long = 0,

    // Spaced Repetition
    val repetition: Int = 0,
    val easinessFactor: Float = 2.5f,
    val interval: Int = 0,
    val nextReviewDate: Long = 0
)
