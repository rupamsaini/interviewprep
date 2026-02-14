package com.rupamsaini.interviewprep.domain.util

import com.rupamsaini.interviewprep.domain.model.Question
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.roundToInt

class SpacedRepetitionCalculator @Inject constructor() {

    /**
     * Calculates the next review schedule using the SuperMemo-2 (SM-2) algorithm.
     *
     * @param question The question being reviewed.
     * @param quality The user's rating of the memory recall quality (0-5).
     *                0-2: Fail (Forgot)
     *                3: Hard
     *                4: Good
     *                5: Easy
     * @return A copy of the Question with updated scheduling fields.
     */
    fun calculateNextReview(question: Question, quality: Int): Question {
        // q: user grade (0-5)
        // n: repetition number
        // EF: easiness factor
        // I: interval in days
        
        var newRepetition = question.repetition
        var newEasinessFactor = question.easinessFactor
        var newInterval: Int
        
        if (quality >= 3) {
            // Correct response
            if (newRepetition == 0) {
                newInterval = 1
            } else if (newRepetition == 1) {
                newInterval = 6
            } else {
                newInterval = (question.interval * newEasinessFactor).roundToInt()
            }
            newRepetition += 1
        } else {
            // Incorrect response / Forgot
            newRepetition = 0
            newInterval = 1
        }
        
        // Calculate new EF
        // EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        newEasinessFactor += (0.1f - (5 - quality) * (0.08f + (5 - quality) * 0.02f))
        
        // EF cannot fall below 1.3
        newEasinessFactor = max(1.3f, newEasinessFactor)
        
        val newNextReviewDate = calculateDateFromNow(newInterval)
        
        return question.copy(
            repetition = newRepetition,
            easinessFactor = newEasinessFactor,
            interval = newInterval,
            nextReviewDate = newNextReviewDate
        )
    }

    private fun calculateDateFromNow(daysToAdd: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
        return calendar.timeInMillis
    }
}
