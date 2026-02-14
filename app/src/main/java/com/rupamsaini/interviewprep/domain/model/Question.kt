package com.rupamsaini.interviewprep.domain.model

data class Question(
    val id: Long = 0,
    val question: String,
    val answer: String,
    val category: String,
    val difficulty: String, // "junior", "mid", "senior"
    val source: String, // "local", "ai", "scraped"
    val explanation: String? = null,
    val codeExample: String? = null,
    val lastShown: Long = 0,
    val userRating: Int = 0, // 1-5

    // Spaced Repetition (SM-2)
    val repetition: Int = 0,
    val easinessFactor: Float = 2.5f,
    val interval: Int = 0, // In days
    val nextReviewDate: Long = 0 // Timestamp
)
