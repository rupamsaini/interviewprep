package com.rupamsaini.interviewprep.domain.manager

interface NotificationScheduler {
    fun scheduleDailyNotification(hour: Int, minute: Int)
    fun cancelDailyNotification()
    fun scheduleDailyDeletion(hour: Int, minute: Int)
    fun cancelDailyDeletion()
}
