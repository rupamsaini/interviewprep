package com.rupamsaini.interviewprep.data.manager

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.rupamsaini.interviewprep.data.worker.DeleteWorker
import com.rupamsaini.interviewprep.data.worker.NotificationWorker
import com.rupamsaini.interviewprep.domain.manager.NotificationScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerNotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) : NotificationScheduler {

    private val workManager = WorkManager.getInstance(context)

    override fun scheduleDailyNotification(hour: Int, minute: Int) {
        val initialDelay = calculateDelayUntil(hour, minute)

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "daily_question_notification",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    override fun cancelDailyNotification() {
        workManager.cancelUniqueWork("daily_question_notification")
    }

    override fun scheduleDailyDeletion(hour: Int, minute: Int) {
        val initialDelay = calculateDelayUntil(hour, minute)

        val workRequest = PeriodicWorkRequestBuilder<DeleteWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "daily_question_deletion",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    override fun cancelDailyDeletion() {
        workManager.cancelUniqueWork("daily_question_deletion")
    }

    private fun calculateDelayUntil(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }

        return target.timeInMillis - now.timeInMillis
    }
}
