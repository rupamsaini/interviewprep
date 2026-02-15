package com.rupamsaini.interviewprep.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.rupamsaini.interviewprep.data.local.AppDatabase
import com.rupamsaini.interviewprep.data.local.QuestionDao
import com.rupamsaini.interviewprep.data.repository.QuestionRepositoryImpl
import com.rupamsaini.interviewprep.domain.repository.QuestionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        callback: AppDatabase.Callback
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .addCallback(callback)
        .addMigrations(AppDatabase.MIGRATION_2_3)
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    @Singleton
    fun provideQuestionDao(db: AppDatabase): QuestionDao {
        return db.questionDao()
    }

}
