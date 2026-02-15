package com.rupamsaini.interviewprep.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rupamsaini.interviewprep.data.local.entity.QuestionEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [QuestionEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun questionDao(): QuestionDao

    companion object {
        const val DATABASE_NAME = "interview_prep_db"

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE questions ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
            }
        }
    }

    class Callback @Inject constructor(
        private val database: Provider<AppDatabase>,
        @param:ApplicationContext private val context: Context
    ) : RoomDatabase.Callback() {
        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            scope.launch {
                populateDatabase()
            }
        }

        private suspend fun populateDatabase() {
            try {
                val inputStream = context.assets.open("questions.json")
                val reader = BufferedReader(InputStreamReader(inputStream))
                val jsonString = reader.use { it.readText() }

                val questions = Json.decodeFromString<List<QuestionDto>>(jsonString)
                val entities = questions.map { it.toEntity() }

                database.get().questionDao().insertAll(entities)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
