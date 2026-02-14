package com.rupamsaini.interviewprep.data.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rupamsaini.interviewprep.MainActivity
import com.rupamsaini.interviewprep.R
import com.rupamsaini.interviewprep.data.preferences.UserPreferencesRepository
import com.rupamsaini.interviewprep.domain.repository.QuestionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.Calendar

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val questionRepository: QuestionRepository,
    private val preferencesRepository: UserPreferencesRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (!preferencesRepository.dailyNotificationEnabled.first()) return Result.success()

        val isWeekendMode = preferencesRepository.weekendModeEnabled.first()
        if (isWeekendMode && isWeekend()) {
            return Result.success()
        }

        val questions = questionRepository.getQuestions().first()
        if (questions.isNotEmpty()) {
            val randomQuestion = questions.random()
            showNotification(randomQuestion.id, randomQuestion.question)
        }

        return Result.success()
    }

    private fun isWeekend(): Boolean {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_WEEK)
        return day == Calendar.SATURDAY || day == Calendar.SUNDAY
    }

    private fun showNotification(questionId: Long, questionText: String) {
        val channelId = "daily_question"
        val notificationId = 1001

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Daily Question"
            val descriptionText = "Daily interview preparation question"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // We can handle deep linking via Intent extras or data. 
            // For simplicitly using extra which MainActivity/NavGraph can parse if we add logic.
            // Or better, let's keep it simple: opens app home. Ideally deep link uri.
            // data = Uri.parse("interviewprep://question/$questionId")
        }
        
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use launcher foreground as fallback icon
            .setContentTitle("Daily Interview Question")
            .setContentText(questionText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        }
    }
}
