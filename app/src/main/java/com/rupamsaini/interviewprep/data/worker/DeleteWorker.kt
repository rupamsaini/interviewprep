package com.rupamsaini.interviewprep.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rupamsaini.interviewprep.data.preferences.UserPreferencesRepository
import com.rupamsaini.interviewprep.domain.repository.QuestionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class DeleteWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val questionRepository: QuestionRepository,
    private val preferencesRepository: UserPreferencesRepository
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "DeleteWorker"
    }

    override suspend fun doWork(): Result {
        return try {
            val scope = preferencesRepository.autoDeleteScope.first()
            val deletedCount = questionRepository.deleteQuestions(scope)
            Log.d(TAG, "Deleted $deletedCount questions with scope: $scope")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete questions", e)
            Result.failure()
        }
    }
}
