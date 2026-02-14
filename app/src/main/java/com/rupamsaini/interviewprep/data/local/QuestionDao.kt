package com.rupamsaini.interviewprep.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rupamsaini.interviewprep.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions")
    fun getAllQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun getQuestionById(id: Long): QuestionEntity?

    @Query("SELECT * FROM questions WHERE category = :category")
    suspend fun getQuestionsByCategory(category: String): List<QuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(question: QuestionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<QuestionEntity>)

    @Update
    suspend fun update(question: QuestionEntity)

    @Query("SELECT * FROM questions ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuestion(): QuestionEntity?

    @Query("SELECT * FROM questions WHERE category = :category ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuestionByCategory(category: String): QuestionEntity?

    @Query("SELECT * FROM questions WHERE difficulty = :difficulty ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuestionByDifficulty(difficulty: String): QuestionEntity?

    @Query("SELECT * FROM questions WHERE category = :category AND difficulty = :difficulty ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuestionByCategoryAndDifficulty(category: String, difficulty: String): QuestionEntity?

    @Delete
    suspend fun delete(question: QuestionEntity)
}
